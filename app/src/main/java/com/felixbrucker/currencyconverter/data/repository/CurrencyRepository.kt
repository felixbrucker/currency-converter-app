package com.felixbrucker.currencyconverter.data.repository

import android.content.Context
import android.util.Log
import com.felixbrucker.currencyconverter.data.CurrenciesCatalog
import com.felixbrucker.currencyconverter.data.local.AppDatabase
import com.felixbrucker.currencyconverter.data.local.AppSettingEntity
import com.felixbrucker.currencyconverter.data.local.ExchangeRateProviderEntity
import com.felixbrucker.currencyconverter.data.local.ExchangeRateEntity
import com.felixbrucker.currencyconverter.data.local.UserCurrencyEntity
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.data.remote.LatestRatesResponse
import com.felixbrucker.currencyconverter.data.remote.provider.CoinGecko
import com.felixbrucker.currencyconverter.data.remote.provider.ExchangeRateApi
import com.felixbrucker.currencyconverter.data.remote.provider.Frankfurter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock.System.now

class CurrencyRepository(
    database: AppDatabase
) {
    private val dao = database.currencyDao()

    private val exchangeRateProviders = mapOf<String, ExchangeRateProvider>(
        ExchangeRateApi.NAME to ExchangeRateApi(),
        Frankfurter.NAME to Frankfurter(),
        CoinGecko.NAME to CoinGecko(),
    )

    companion object {
        private const val TAG = "CurrencyRepository"
        const val KEY_BG_SYNC_ENABLED = "setting_bg_sync_enabled"
        const val KEY_BG_SYNC_INTERVAL_HOURS = "setting_bg_sync_interval_hours"
        const val KEY_AUTO_REFRESH_MINUTES = "setting_auto_refresh_minutes"
        const val KEY_LAST_SYNC_TIME = "setting_last_sync_time"
        const val KEY_ACTIVE_CURRENCY_CODE = "setting_active_currency_code"
        const val KEY_ACTIVE_INPUT_AMOUNT = "setting_active_input_amount"

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

    val providersFlow: Flow<List<Pair<ExchangeRateProviderEntity, ExchangeRateProvider>>> = dao.getAllProvidersFlow().map { providers ->
        providers.mapNotNull { provider ->
            val exchangeRateProvider = exchangeRateProviders[provider.name] ?: return@mapNotNull null
            Pair(provider, exchangeRateProvider)
        }
    }

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

        val providers = dao.getAllProvidersFlow().firstOrNull() ?: emptyList()
        val providerNames = providers.map { it.name }.toSet()
        val missingProviders = mutableListOf<ExchangeRateProviderEntity>()
        exchangeRateProviders.values.forEach {
            if (!providerNames.contains(it.name)) {
                missingProviders.add(ExchangeRateProviderEntity(it.name, it.defaultEnabled))
            }
        }
        if (missingProviders.isNotEmpty()) {
            dao.insertProviders(missingProviders)
        }
        val legacyProviders = providerNames.filter { !exchangeRateProviders.containsKey(it) }
        dao.deleteProviders(legacyProviders)

        val bgEnabled = dao.getSetting(KEY_BG_SYNC_ENABLED)
        if (bgEnabled == null) {
            dao.setSetting(AppSettingEntity(KEY_BG_SYNC_ENABLED, "false"))
            dao.setSetting(AppSettingEntity(KEY_BG_SYNC_INTERVAL_HOURS, "12"))
            dao.setSetting(AppSettingEntity(KEY_AUTO_REFRESH_MINUTES, "5"))
            dao.setSetting(AppSettingEntity(KEY_ACTIVE_CURRENCY_CODE, "USD"))
            dao.setSetting(AppSettingEntity(KEY_ACTIVE_INPUT_AMOUNT, "1.00"))
        }
    }

    suspend fun refreshRates(): Result<Int> = withContext(Dispatchers.IO) {
        val now = now()
        try {
            val eligibleProviders = dao.getEligibleProvidersForSync(now = now)
            if (eligibleProviders.isEmpty()) {
                return@withContext Result.success(0)
            }
            val freshRateEntities = mutableMapOf<String, ExchangeRateEntity>()
            var anySuccess = false

            coroutineScope {
                val deferreds = eligibleProviders.map { providerEntity ->
                    async {
                        val provider = exchangeRateProviders[providerEntity.name] ?: return@async null
                        val response: LatestRatesResponse
                        try {
                            response = provider.getLatestUsdRates()
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch from ${provider.name}: ${e.message}")

                            return@async null
                        }
                        if (response.rates.isEmpty()) {
                            return@async null
                        }
                        dao.updateProviderSyncTimes(
                            name = provider.name,
                            lastUpdatedAt = response.updatedAt,
                            nextUpdateAt = response.nextUpdateAt,
                        )

                        response
                    }
                }

                // Update/insert all rate entities which have newer rates from the api
                val existingRateEntities = (dao.getAllRates().firstOrNull() ?: emptyList()).associateBy { it.code }
                deferreds.awaitAll().filterNotNull().forEach { response ->
                    response.rates.forEach { (code, rate) ->
                        val entity = ExchangeRateEntity(
                            code = code.uppercase(),
                            rateToUsd = rate,
                            lastUpdatedAt = response.updatedAt
                        )
                        val existingRateEntity = existingRateEntities[entity.code]
                        if (existingRateEntity != null && existingRateEntity.lastUpdatedAt >= entity.lastUpdatedAt) {
                            return@forEach
                        }
                        val existingFreshRateEntity = freshRateEntities[entity.code]
                        if (existingFreshRateEntity == null || existingFreshRateEntity.lastUpdatedAt < entity.lastUpdatedAt) {
                            freshRateEntities[entity.code] = entity
                        }
                    }
                    anySuccess = true
                }
            }

            if (anySuccess) {
                val entities = freshRateEntities.values.toList()
                dao.insertRates(entities)
                dao.setSetting(AppSettingEntity(KEY_LAST_SYNC_TIME, now.toEpochMilliseconds().toString()))
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

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.setSetting(AppSettingEntity(key, value))
    }

    suspend fun getSetting(key: String): String? = dao.getSetting(key)?.value
}
