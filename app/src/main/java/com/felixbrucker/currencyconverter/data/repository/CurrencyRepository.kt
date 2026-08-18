package com.felixbrucker.currencyconverter.data.repository

import android.content.Context
import android.util.Log
import com.felixbrucker.currencyconverter.data.CurrenciesCatalog
import com.felixbrucker.currencyconverter.data.local.AppDatabase
import com.felixbrucker.currencyconverter.data.local.AppSettingEntity
import com.felixbrucker.currencyconverter.data.local.CurrencyProviderEntity
import com.felixbrucker.currencyconverter.data.local.ExchangeRateEntity
import com.felixbrucker.currencyconverter.data.local.UserCurrencyEntity
import com.felixbrucker.currencyconverter.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CurrencyRepository(
    private val database: AppDatabase
) {
    private val dao = database.currencyDao()

    companion object {
        private const val TAG = "CurrencyRepository"
        const val KEY_BG_SYNC_ENABLED = "setting_bg_sync_enabled"
        const val KEY_BG_SYNC_INTERVAL_HOURS = "setting_bg_sync_interval_hours"
        const val KEY_AUTO_REFRESH_MINUTES = "setting_auto_refresh_minutes"
        const val KEY_LAST_SYNC_TIME = "setting_last_sync_time"

        const val PROVIDER_OPEN_ER = "Open ER API"
        const val PROVIDER_FRANKFURTER = "Frankfurter API"

        @Volatile
        private var INSTANCE: CurrencyRepository? = null

        fun getInstance(context: Context): CurrencyRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val repo = CurrencyRepository(db)
                INSTANCE = repo
                repo
            }
        }
    }

    val ratesFlow: Flow<Map<String, ExchangeRateEntity>> = dao.getAllRates().map { entities ->
        entities.associateBy { it.code }
    }

    val userCurrenciesFlow: Flow<List<UserCurrencyEntity>> = dao.getUserCurrencies()

    val providersFlow: Flow<List<CurrencyProviderEntity>> = dao.getAllProvidersFlow()

    val lastUpdatedFlow: Flow<Long> = dao.getSettingFlow(KEY_LAST_SYNC_TIME).map { entity ->
        entity?.value?.toLongOrNull() ?: 0L
    }

    suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
        val existingUserCurrencies = dao.getUserCurrencies().firstOrNull()
        if (existingUserCurrencies.isNullOrEmpty()) {
            val initialUserCurrencies = CurrenciesCatalog.defaultSelectedCodes.mapIndexed { index, code ->
                UserCurrencyEntity(code = code, displayOrder = index, isSelected = true)
            }
            dao.insertUserCurrencies(initialUserCurrencies)
        }

        val providers = dao.getAllProvidersFlow().firstOrNull()
        if (providers.isNullOrEmpty()) {
            val initialProviders = listOf(
                CurrencyProviderEntity(PROVIDER_OPEN_ER, true, 0),
                CurrencyProviderEntity(PROVIDER_FRANKFURTER, false, 1),
            )
            dao.insertProviders(initialProviders)
        }

        val bgEnabled = dao.getSetting(KEY_BG_SYNC_ENABLED)
        if (bgEnabled == null) {
            dao.setSetting(AppSettingEntity(KEY_BG_SYNC_ENABLED, "false"))
            dao.setSetting(AppSettingEntity(KEY_BG_SYNC_INTERVAL_HOURS, "12"))
            dao.setSetting(AppSettingEntity(KEY_AUTO_REFRESH_MINUTES, "5"))
        }
    }

    suspend fun refreshRates(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val enabledProviders = dao.getEnabledProviders()
            if (enabledProviders.isEmpty()) {
                return@withContext Result.failure(Exception("No exchange rate providers enabled"))
            }

            // We sort providers by displayOrder DESCENDING so that during merging,
            // lower displayOrder (higher priority) providers overwrite values from lower priority ones.
            val sortedProviders = enabledProviders.sortedByDescending { it.displayOrder }
            val consolidatedRates = mutableMapOf<String, Double>()
            var anySuccess = false
            val now = System.currentTimeMillis()

            coroutineScope {
                val deferreds = sortedProviders.map { provider ->
                    async {
                        try {
                            val rates = when (provider.name) {
                                PROVIDER_OPEN_ER -> {
                                    val response = NetworkClient.openErApi.getLatestRates()
                                    response.rates
                                }
                                PROVIDER_FRANKFURTER -> {
                                    val response = NetworkClient.frankfurterApi.getLatestRates("USD")
                                    response.rates
                                }
                                else -> emptyMap()
                            }

                            if (rates.isNotEmpty()) {
                                dao.updateProviderSyncTime(provider.name, now)
                                provider.name to rates
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch from ${provider.name}: ${e.message}")
                            null
                        }
                    }
                }

                deferreds.awaitAll().filterNotNull().forEach { (providerName, rates) ->
                    consolidatedRates.putAll(rates)
                    anySuccess = true
                }
            }

            if (anySuccess) {
                consolidatedRates["USD"] = 1.0
                val entities = consolidatedRates.map { (code, rate) ->
                    ExchangeRateEntity(code = code.uppercase(), rateToUsd = rate, lastUpdated = now)
                }
                dao.insertRates(entities)
                dao.setSetting(AppSettingEntity(KEY_LAST_SYNC_TIME, now.toString()))
                Result.success(entities.size)
            } else {
                Result.failure(Exception("No exchange rates retrieved from servers"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing rates", e)
            Result.failure(e)
        }
    }

    suspend fun toggleProvider(name: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.updateProviderStatus(name, enabled)
    }

    suspend fun updateProvidersOrder(names: List<String>) = withContext(Dispatchers.IO) {
        val existing = dao.getAllProvidersFlow().firstOrNull() ?: emptyList()
        val existingMap = existing.associateBy { it.name }
        val updated = names.mapIndexedNotNull { index, name ->
            existingMap[name]?.copy(displayOrder = index)
        }
        dao.insertProviders(updated)
    }


    suspend fun toggleCurrencySelection(code: String, isSelected: Boolean) = withContext(Dispatchers.IO) {
        val current = dao.getUserCurrencies().firstOrNull() ?: emptyList()
        val existing = current.find { it.code.equals(code, ignoreCase = true) }
        if (existing != null) {
            dao.toggleCurrencySelection(code.uppercase(), isSelected)
        } else if (isSelected) {
            val nextOrder = (current.maxOfOrNull { it.displayOrder } ?: -1) + 1
            dao.insertUserCurrencies(
                listOf(UserCurrencyEntity(code = code.uppercase(), displayOrder = nextOrder, isSelected = true))
            )
        }
    }

    suspend fun updateCurrenciesOrder(orderedCodes: List<String>) = withContext(Dispatchers.IO) {
        val current = dao.getUserCurrencies().firstOrNull() ?: emptyList()
        val currentMap = current.associateBy { it.code }
        val updated = orderedCodes.mapIndexed { index, code ->
            val isSelected = currentMap[code]?.isSelected ?: true
            UserCurrencyEntity(code = code, displayOrder = index, isSelected = isSelected)
        }
        // Include any unselected ones that were not in orderedCodes
        val remaining = current.filter { !orderedCodes.contains(it.code) }
        val allUpdated = updated + remaining.mapIndexed { idx, item ->
            item.copy(displayOrder = updated.size + idx)
        }
        dao.replaceUserCurrencies(allUpdated)
    }

    suspend fun removeCurrency(code: String) = withContext(Dispatchers.IO) {
        dao.toggleCurrencySelection(code, false)
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.setSetting(AppSettingEntity(key, value))
    }

    fun getSettingFlow(key: String): Flow<String?> = dao.getSettingFlow(key).map { it?.value }

    suspend fun getSetting(key: String): String? = dao.getSetting(key)?.value
}
