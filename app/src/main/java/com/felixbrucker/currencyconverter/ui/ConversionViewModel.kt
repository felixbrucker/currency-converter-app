package com.felixbrucker.currencyconverter.ui

import android.app.Application
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.felixbrucker.currencyconverter.data.CurrenciesCatalog
import com.felixbrucker.currencyconverter.data.local.ExchangeRateProviderEntity
import com.felixbrucker.currencyconverter.data.local.ExchangeRateEntity
import com.felixbrucker.currencyconverter.data.local.UserCurrencyEntity
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.data.repository.CurrencyRepository
import com.felixbrucker.currencyconverter.data.worker.ExchangeRateSyncWorker
import com.felixbrucker.currencyconverter.extensions.aggregate
import com.felixbrucker.currencyconverter.model.ConversionRowState
import com.felixbrucker.currencyconverter.model.Currency
import com.felixbrucker.currencyconverter.model.CurrencyType
import com.felixbrucker.currencyconverter.util.CurrencyFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.text.lowercase
import kotlin.text.uppercase
import kotlin.time.Clock.System.now
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

data class ConversionUiState(
    val rows: List<ConversionRowState> = emptyList(),
    val activeCurrencyCode: String = "USD",
    val activeInputText: String = "",
    val isHintActive: Boolean = true,
    val lastUpdatedTimestamp: Long = 0L,
    val isRefreshing: Boolean = false,
    val maxCountdownSeconds: Int = 300,
    val isOnline: Boolean = true,
    val bgSyncEnabled: Boolean = true,
    val bgSyncIntervalHours: Long = 12L,
    val autoRefreshMinutes: Int = 5,
    val searchQuery: String = "",
    val providers: List<Pair<ExchangeRateProviderEntity, ExchangeRateProvider>> = emptyList()
)

class ConversionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurrencyRepository.getInstance(application)

    sealed class UiEvent {
        data class ShowSnackbar(val message: String, val duration: SnackbarDuration = SnackbarDuration.Short) : UiEvent()
    }

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    private val _activeCurrencyCode = MutableStateFlow("USD")
    private val _activeHintAmount = MutableStateFlow("1.00")
    private val _activeInputText = MutableStateFlow("")
    private val _isHintActive = MutableStateFlow(true)

    private val _isRefreshing = MutableStateFlow(false)
    private val _isOnline = MutableStateFlow(true)

    private val _searchQuery = MutableStateFlow("")

    private val _bgSyncEnabled = MutableStateFlow(false)
    private val _bgSyncIntervalHours = MutableStateFlow(12L)
    private val _autoRefreshMinutes = MutableStateFlow(5)

    private val _countdownSeconds = MutableStateFlow(300)
    private val _maxCountdownSeconds = MutableStateFlow(300)

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty()
            loadSettings()
            refreshRates(showLoadingIndicator = true)
            startCountdownTimer()
        }
    }

    private suspend fun loadSettings() {
        val bgEn = repository.getSetting(CurrencyRepository.KEY_BG_SYNC_ENABLED)?.toBooleanStrictOrNull() ?: false
        val bgHours = repository.getSetting(CurrencyRepository.KEY_BG_SYNC_INTERVAL_HOURS)?.toLongOrNull() ?: 12L
        val autoMins = repository.getSetting(CurrencyRepository.KEY_AUTO_REFRESH_MINUTES)?.toIntOrNull() ?: 5

        val activeCode = repository.getSetting(CurrencyRepository.KEY_ACTIVE_CURRENCY_CODE) ?: "USD"
        val activeAmount = repository.getSetting(CurrencyRepository.KEY_ACTIVE_INPUT_AMOUNT) ?: "1.00"

        _bgSyncEnabled.value = bgEn
        _bgSyncIntervalHours.value = bgHours
        _autoRefreshMinutes.value = autoMins

        _activeCurrencyCode.value = activeCode
        _activeHintAmount.value = activeAmount
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val totalSeconds = (_autoRefreshMinutes.value * 60).coerceAtLeast(5)
                _maxCountdownSeconds.value = totalSeconds
                _countdownSeconds.value = totalSeconds

                while (_countdownSeconds.value > 0) {
                    delay(1.seconds)
                    _countdownSeconds.value -= 1
                }
                refreshRates(showLoadingIndicator = false)
            }
        }
    }

    val allCurrenciesWithSelection: StateFlow<List<Pair<Currency, Boolean>>> = combine(
        _searchQuery,
        repository.userCurrenciesFlow,
    ) { params: Array<Any?> ->
        var idx = 0
        val search = params[idx++] as String
        @Suppress("UNCHECKED_CAST")
        val userCurrencies = params[idx++] as List<UserCurrencyEntity>
        val selectedCodeSet = userCurrencies.filter { it.isSelected }.map { it.code.uppercase() }.toSet()
        val allCurrenciesWithFlags = CurrenciesCatalog.allCurrencies.map { c ->
            c to selectedCodeSet.contains(c.code.uppercase())
        }

        if (search.isBlank()) {
            val (selected, unselected) = allCurrenciesWithFlags.partition { it.second }
            selected + unselected
        } else {
            val q = search.trim().lowercase()
            allCurrenciesWithFlags.filter { (c, _) ->
                c.code.lowercase().contains(q) ||
                        c.name.lowercase().contains(q) ||
                        when (c.type) {
                            is CurrencyType.Fiat -> c.type.country.lowercase().contains(q)
                            is CurrencyType.Crypto -> c.type.coinGeckoId.lowercase().contains(q)
                        }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf()
    )

    val countdownSeconds: StateFlow<Int> = _countdownSeconds.asStateFlow()

    val uiState: StateFlow<ConversionUiState> = combine(
        repository.userCurrenciesFlow,
        repository.ratesFlow,
        repository.lastUpdatedFlow,
        _isRefreshing,
        _isOnline,
        _bgSyncEnabled,
        _bgSyncIntervalHours,
        _autoRefreshMinutes,
        _activeCurrencyCode,
        _activeHintAmount,
        _activeInputText,
        _isHintActive,
        _maxCountdownSeconds,
        _searchQuery,
        repository.providersFlow
    ) { params: Array<Any?> ->
        var idx = 0
        @Suppress("UNCHECKED_CAST")
        val userCurrencies = params[idx++] as List<UserCurrencyEntity>
        @Suppress("UNCHECKED_CAST")
        val rates = params[idx++] as Map<String, ExchangeRateEntity>
        val lastUpdated = params[idx++] as Long
        val isRef = params[idx++] as Boolean
        val isOnline = params[idx++] as Boolean
        val bgSyncEn = params[idx++] as Boolean
        val bgSyncHrs = params[idx++] as Long
        val autoMins = params[idx++] as Int
        val activeCode = params[idx++] as String
        val activeHint = params[idx++] as String
        val activeInput = params[idx++] as String
        val isHint = params[idx++] as Boolean
        val maxCountdown = params[idx++] as Int
        val search = params[idx++] as String
        @Suppress("UNCHECKED_CAST")
        val providers = params[idx++] as List<Pair<ExchangeRateProviderEntity, ExchangeRateProvider>>

        val selectedUserCurrencies = userCurrencies
            .filter { it.isSelected }
            .sortedBy { it.displayOrder }

        val activeCurrency = CurrenciesCatalog.find(activeCode)
        val activeRateEntity = rates[activeCode.uppercase()]
        val activeRateToUsd = activeRateEntity?.rateToUsd

        val effectiveAmount: Double = if (isHint || activeInput.isBlank()) {
            activeHint.replace(",", "").toDoubleOrNull() ?: 1.0
        } else {
            activeInput.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val now = now()
        val staleThreshold = 24.hours

        val rows = selectedUserCurrencies.mapNotNull { userCurrency ->
            val currency = CurrenciesCatalog.find(userCurrency.code) ?: return@mapNotNull null
            val isFocused = currency.code.equals(activeCode, ignoreCase = true)
            val rateEntity = rates[currency.code.uppercase()]
            val currencyRateToUsd = rateEntity?.rateToUsd
            val isStale = rateEntity != null && (now.minus(rateEntity.lastUpdatedAt) > staleThreshold)
            val isRateUnavailable = rateEntity == null

            val convertedAmount = if (activeRateToUsd != null && currencyRateToUsd != null && activeRateToUsd > 0) {
                effectiveAmount * (currencyRateToUsd / activeRateToUsd)
            } else {
                null
            }

            val unitExchangeRate = if (activeRateToUsd != null && currencyRateToUsd != null && activeRateToUsd > 0) {
                currencyRateToUsd / activeRateToUsd
            } else {
                null
            }

            val displayedText = if (isFocused) {
                activeInput.ifBlank { CurrencyFormatter.formatAmount(effectiveAmount, currency) }
            } else {
                if (convertedAmount != null) {
                    CurrencyFormatter.formatAmount(convertedAmount, currency)
                } else {
                    "N/A"
                }
            }

            val hintText = if (isFocused) {
                activeInput.ifBlank { CurrencyFormatter.formatAmount(effectiveAmount, currency) }
            } else {
                if (convertedAmount != null) {
                    CurrencyFormatter.formatAmount(convertedAmount, currency)
                } else {
                    "N/A"
                }
            }

            val rateFormatted = if (unitExchangeRate != null) {
                CurrencyFormatter.formatRate(unitExchangeRate, currency)
            } else {
                "N/A"
            }
            val baseRateText = if (unitExchangeRate != null) {
                "1 ${activeCurrency?.code ?: activeCode.uppercase()} = $rateFormatted ${currency.code}"
            } else {
                "Rate unavailable"
            }

            ConversionRowState(
                currency = currency,
                isFocused = isFocused,
                enteredText = if (isFocused) activeInput else "",
                displayedAmountText = displayedText,
                hintAmountText = hintText,
                isHintActive = isFocused && isHint,
                baseExchangeRateText = baseRateText,
                displayOrder = userCurrency.displayOrder,
                isStale = isStale,
                isRateUnavailable = isRateUnavailable
            )
        }

        ConversionUiState(
            rows = rows,
            activeCurrencyCode = activeCode,
            activeInputText = activeInput,
            isHintActive = isHint,
            lastUpdatedTimestamp = lastUpdated,
            isRefreshing = isRef,
            maxCountdownSeconds = maxCountdown,
            isOnline = isOnline,
            bgSyncEnabled = bgSyncEn,
            bgSyncIntervalHours = bgSyncHrs,
            autoRefreshMinutes = autoMins,
            searchQuery = search,
            providers = providers,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConversionUiState()
    )

    fun onRowFocused(code: String) {
        val currentState = uiState.value
        val targetRow = currentState.rows.find { it.currency.code.equals(code, ignoreCase = true) }
        val currentAmountText = targetRow?.displayedAmountText?.replace(",", "") ?: "1.00"

        val parsed = currentAmountText.toDoubleOrNull() ?: 1.0
        val cleanAmount = if (parsed <= 0.0) 1.0 else parsed
        val activeCurrency = CurrenciesCatalog.find(code) ?: return
        val formatted = CurrencyFormatter.formatAmount(cleanAmount, activeCurrency)

        val newHint = formatted.replace(",", "")
        _activeCurrencyCode.value = code
        _activeHintAmount.value = newHint
        _activeInputText.value = ""
        _isHintActive.value = true

        viewModelScope.launch {
            repository.setSetting(CurrencyRepository.KEY_ACTIVE_CURRENCY_CODE, code)
            repository.setSetting(CurrencyRepository.KEY_ACTIVE_INPUT_AMOUNT, newHint)
        }
    }

    fun onAmountInputChanged(input: String) {
        val cleaned = CurrencyFormatter.cleanInput(input)
        _activeInputText.value = cleaned
        _isHintActive.value = cleaned.isBlank()

        viewModelScope.launch {
            if (cleaned.isNotBlank()) {
                repository.setSetting(CurrencyRepository.KEY_ACTIVE_INPUT_AMOUNT, cleaned)
            }
        }
    }

    fun onFinishInput() {
        val currentInput = _activeInputText.value.trim()
        if (currentInput.isNotBlank()) {
            val parsed = currentInput.toDoubleOrNull()
            if (parsed != null && parsed > 0.0) {
                CurrenciesCatalog
                    .find(_activeCurrencyCode.value)
                    ?.let { activeCurrency ->
                        val formatted = CurrencyFormatter.formatAmount(parsed, activeCurrency)
                        val newHint = formatted.replace(",", "")
                        _activeHintAmount.value = newHint
                        viewModelScope.launch {
                            repository.setSetting(CurrencyRepository.KEY_ACTIVE_INPUT_AMOUNT, newHint)
                        }
                    }
            }
        }
        _activeInputText.value = ""
        _isHintActive.value = true
    }

    fun onToggleCurrency(code: String, isSelected: Boolean) {
        viewModelScope.launch {
            repository.toggleCurrencySelection(code, isSelected)
        }
    }

    fun onReorderCurrencies(newOrderedCodes: List<String>) {
        viewModelScope.launch {
            repository.updateCurrenciesOrder(newOrderedCodes)
        }
    }

    fun onReorder(fromIndex: Int, toIndex: Int) {
        val currentRows = uiState.value.rows
        if (fromIndex !in currentRows.indices || toIndex !in currentRows.indices) return
        val updated = currentRows.map { it.currency.code }.toMutableList()
        val item = updated.removeAt(fromIndex)
        updated.add(toIndex, item)
        onReorderCurrencies(updated)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onToggleProvider(name: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleProvider(name, isEnabled)
        }
    }

    fun refreshRates(showLoadingIndicator: Boolean = true) {
        viewModelScope.launch {
            if (showLoadingIndicator) {
                _isRefreshing.value = true
            }
            val result = repository.refreshRates()
            if (showLoadingIndicator) {
                _isRefreshing.value = false
            }
            if (result.isSuccess) {
                val syncResult = result.getOrThrow()
                _isOnline.value = true
                if (syncResult.providerErrors.isNotEmpty()) {
                    val error = syncResult.providerErrors.aggregate()
                    _uiEvent.emit(UiEvent.ShowSnackbar("Partial sync: ${error.message}", duration = SnackbarDuration.Long))
                }
            } else {
                _isOnline.value = false
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiEvent.emit(UiEvent.ShowSnackbar("Sync failed: $error", duration = SnackbarDuration.Long))
            }
            _countdownSeconds.value = _maxCountdownSeconds.value
        }
    }

    fun setBgSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _bgSyncEnabled.value = enabled
            repository.setSetting(CurrencyRepository.KEY_BG_SYNC_ENABLED, enabled.toString())
            ExchangeRateSyncWorker.schedule(getApplication(), _bgSyncIntervalHours.value, enabled)
        }
    }

    fun setBgSyncIntervalHours(hours: Long) {
        viewModelScope.launch {
            _bgSyncIntervalHours.value = hours
            repository.setSetting(CurrencyRepository.KEY_BG_SYNC_INTERVAL_HOURS, hours.toString())
            ExchangeRateSyncWorker.schedule(getApplication(), hours, _bgSyncEnabled.value)
        }
    }

    fun setAutoRefreshMinutes(minutes: Int) {
        viewModelScope.launch {
            _autoRefreshMinutes.value = minutes
            repository.setSetting(CurrencyRepository.KEY_AUTO_REFRESH_MINUTES, minutes.toString())
            startCountdownTimer()
        }
    }
}
