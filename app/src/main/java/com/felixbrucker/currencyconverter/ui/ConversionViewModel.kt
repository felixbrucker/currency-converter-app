package com.felixbrucker.currencyconverter.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.felixbrucker.currencyconverter.data.CurrenciesCatalog
import com.felixbrucker.currencyconverter.data.local.UserCurrencyEntity
import com.felixbrucker.currencyconverter.data.repository.CurrencyRepository
import com.felixbrucker.currencyconverter.data.worker.ExchangeRateSyncWorker
import com.felixbrucker.currencyconverter.model.ConversionRowState
import com.felixbrucker.currencyconverter.model.Currency
import com.felixbrucker.currencyconverter.util.CurrencyFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConversionUiState(
    val rows: List<ConversionRowState> = emptyList(),
    val activeCurrencyCode: String = "USD",
    val activeInputText: String = "",
    val isHintActive: Boolean = true,
    val lastUpdatedTimestamp: Long = 0L,
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val countdownSeconds: Int = 180,
    val maxCountdownSeconds: Int = 180,
    val isOnline: Boolean = true,
    val bgSyncEnabled: Boolean = true,
    val bgSyncIntervalHours: Long = 4L,
    val autoRefreshMinutes: Int = 3,
    val searchQuery: String = "",
    val allCurrenciesWithSelection: List<Pair<Currency, Boolean>> = emptyList()
)

class ConversionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurrencyRepository.getInstance(application)

    private val _activeCurrencyCode = MutableStateFlow("USD")
    private val _activeHintAmount = MutableStateFlow("1.00")
    private val _activeInputText = MutableStateFlow("")
    private val _isHintActive = MutableStateFlow(true)

    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshMessage = MutableStateFlow<String?>(null)
    private val _isOnline = MutableStateFlow(true)

    private val _searchQuery = MutableStateFlow("")

    private val _bgSyncEnabled = MutableStateFlow(true)
    private val _bgSyncIntervalHours = MutableStateFlow(4L)
    private val _autoRefreshMinutes = MutableStateFlow(3)

    private val _countdownSeconds = MutableStateFlow(180)
    private val _maxCountdownSeconds = MutableStateFlow(180)

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty()
            loadSettings()
            startCountdownTimer()
        }
    }

    private suspend fun loadSettings() {
        val bgEn = repository.getSetting(CurrencyRepository.KEY_BG_SYNC_ENABLED)?.toBooleanStrictOrNull() ?: true
        val bgHours = repository.getSetting(CurrencyRepository.KEY_BG_SYNC_INTERVAL_HOURS)?.toLongOrNull() ?: 4L
        val autoMins = repository.getSetting(CurrencyRepository.KEY_AUTO_REFRESH_MINUTES)?.toIntOrNull() ?: 3

        _bgSyncEnabled.value = bgEn
        _bgSyncIntervalHours.value = bgHours
        _autoRefreshMinutes.value = autoMins
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val totalSeconds = (_autoRefreshMinutes.value * 60).coerceAtLeast(5)
                _maxCountdownSeconds.value = totalSeconds
                _countdownSeconds.value = totalSeconds

                while (_countdownSeconds.value > 0) {
                    delay(1000)
                    _countdownSeconds.value -= 1
                }
                refreshRates(showLoadingIndicator = false)
            }
        }
    }

    val uiState: StateFlow<ConversionUiState> = combine(
        repository.userCurrenciesFlow,
        repository.ratesFlow,
        repository.lastUpdatedFlow,
        _isRefreshing,
        _refreshMessage,
        _isOnline,
        _bgSyncEnabled,
        _bgSyncIntervalHours,
        _autoRefreshMinutes,
        _activeCurrencyCode,
        _activeHintAmount,
        _activeInputText,
        _isHintActive,
        _countdownSeconds,
        _maxCountdownSeconds,
        _searchQuery
    ) { params: Array<Any?> ->
        var idx = 0
        @Suppress("UNCHECKED_CAST")
        val userCurrencies = params[idx++] as List<UserCurrencyEntity>
        @Suppress("UNCHECKED_CAST")
        val rates = params[idx++] as Map<String, Double>
        val lastUpdated = params[idx++] as Long
        val isRef = params[idx++] as Boolean
        val refMsg = params[idx++] as String?
        val isOnline = params[idx++] as Boolean
        val bgSyncEn = params[idx++] as Boolean
        val bgSyncHrs = params[idx++] as Long
        val autoMins = params[idx++] as Int
        val activeCode = params[idx++] as String
        val activeHint = params[idx++] as String
        val activeInput = params[idx++] as String
        val isHint = params[idx++] as Boolean
        val countdown = params[idx++] as Int
        val maxCountdown = params[idx++] as Int
        val search = params[idx++] as String

        val selectedUserCurrencies = userCurrencies
            .filter { it.isSelected }
            .sortedBy { it.displayOrder }

        val activeCurrency = CurrenciesCatalog.find(activeCode)
            ?: Currency(activeCode, activeCode, "$", "🌐")
        val activeRateToUsd = rates[activeCode.uppercase()] ?: 1.0

        val effectiveAmount: Double = if (isHint || activeInput.isBlank()) {
            activeHint.replace(",", "").toDoubleOrNull() ?: 1.0
        } else {
            activeInput.replace(",", "").toDoubleOrNull() ?: 0.0
        }

        val rows = selectedUserCurrencies.map { userCurrency ->
            val currency = CurrenciesCatalog.find(userCurrency.code)
                ?: Currency(userCurrency.code, userCurrency.code, "$", "🌐")
            val isFocused = currency.code.equals(activeCode, ignoreCase = true)
            val currencyRateToUsd = rates[currency.code.uppercase()] ?: 1.0

            val convertedAmount = if (activeRateToUsd > 0) {
                effectiveAmount * (currencyRateToUsd / activeRateToUsd)
            } else {
                0.0
            }

            val unitExchangeRate = if (activeRateToUsd > 0) {
                currencyRateToUsd / activeRateToUsd
            } else {
                1.0
            }

            val displayedText = if (isFocused) {
                if (activeInput.isNotBlank()) activeInput
                else CurrencyFormatter.formatAmount(effectiveAmount, currency)
            } else {
                CurrencyFormatter.formatAmount(convertedAmount, currency)
            }

            val hintText = if (isFocused) {
                if (activeInput.isNotBlank()) activeInput
                else CurrencyFormatter.formatAmount(effectiveAmount, currency)
            } else {
                CurrencyFormatter.formatAmount(convertedAmount, currency)
            }

            val rateFormatted = CurrencyFormatter.formatRate(unitExchangeRate, currency)
            val baseRateText = "1 ${activeCurrency.code} = $rateFormatted ${currency.code}"

            ConversionRowState(
                currency = currency,
                isFocused = isFocused,
                enteredText = if (isFocused) activeInput else "",
                displayedAmountText = displayedText,
                hintAmountText = hintText,
                isHintActive = isFocused && isHint,
                baseExchangeRateText = baseRateText,
                displayOrder = userCurrency.displayOrder
            )
        }

        val selectedCodeSet = userCurrencies.filter { it.isSelected }.map { it.code.uppercase() }.toSet()
        val allCurrenciesWithFlags = CurrenciesCatalog.allCurrencies.map { c ->
            c to selectedCodeSet.contains(c.code.uppercase())
        }

        val filteredCurrencies = if (search.isBlank()) {
            val (selected, unselected) = allCurrenciesWithFlags.partition { it.second }
            selected + unselected
        } else {
            val q = search.trim().lowercase()
            allCurrenciesWithFlags.filter { (c, _) ->
                c.code.lowercase().contains(q) ||
                        c.name.lowercase().contains(q) ||
                        c.country.lowercase().contains(q)
            }
        }

        ConversionUiState(
            rows = rows,
            activeCurrencyCode = activeCode,
            activeInputText = activeInput,
            isHintActive = isHint,
            lastUpdatedTimestamp = lastUpdated,
            isRefreshing = isRef,
            refreshMessage = refMsg,
            countdownSeconds = countdown,
            maxCountdownSeconds = maxCountdown,
            isOnline = isOnline,
            bgSyncEnabled = bgSyncEn,
            bgSyncIntervalHours = bgSyncHrs,
            autoRefreshMinutes = autoMins,
            searchQuery = search,
            allCurrenciesWithSelection = filteredCurrencies
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConversionUiState()
    )

    fun onRowFocused(code: String) {
        val currentState = uiState.value
        val targetRow = currentState.rows.find { it.currency.code.equals(code, ignoreCase = true) }
        val currentAmountText = if (targetRow != null) {
            targetRow.displayedAmountText.replace(",", "")
        } else "1.00"

        val parsed = currentAmountText.toDoubleOrNull() ?: 1.0
        val cleanAmount = if (parsed <= 0.0) 1.0 else parsed
        val activeCurrency = CurrenciesCatalog.find(code) ?: Currency(code, code, "$", "🌐")
        val formatted = CurrencyFormatter.formatAmount(cleanAmount, activeCurrency)

        _activeCurrencyCode.value = code
        _activeHintAmount.value = formatted.replace(",", "")
        _activeInputText.value = ""
        _isHintActive.value = true
    }

    fun onAmountInputChanged(input: String) {
        val cleaned = CurrencyFormatter.cleanInput(input)
        _activeInputText.value = cleaned
        _isHintActive.value = cleaned.isBlank()
    }

    fun onFinishInput() {
        val currentInput = _activeInputText.value.trim()
        if (currentInput.isNotBlank()) {
            val parsed = currentInput.toDoubleOrNull()
            if (parsed != null && parsed > 0.0) {
                val activeCurrency = CurrenciesCatalog.find(_activeCurrencyCode.value)
                    ?: Currency(_activeCurrencyCode.value, _activeCurrencyCode.value, "$", "🌐")
                val formatted = CurrencyFormatter.formatAmount(parsed, activeCurrency)
                _activeHintAmount.value = formatted.replace(",", "")
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

    fun onMoveUp(code: String) {
        val currentList = uiState.value.rows.map { it.currency.code }.toMutableList()
        val index = currentList.indexOfFirst { it.equals(code, ignoreCase = true) }
        if (index > 0) {
            val item = currentList.removeAt(index)
            currentList.add(index - 1, item)
            onReorderCurrencies(currentList)
        }
    }

    fun onMoveDown(code: String) {
        val currentList = uiState.value.rows.map { it.currency.code }.toMutableList()
        val index = currentList.indexOfFirst { it.equals(code, ignoreCase = true) }
        if (index >= 0 && index < currentList.lastIndex) {
            val item = currentList.removeAt(index)
            currentList.add(index + 1, item)
            onReorderCurrencies(currentList)
        }
    }

    fun onRemoveCurrency(code: String) {
        onToggleCurrency(code, false)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
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
                _isOnline.value = true
                _refreshMessage.value = "Updated rates successfully"
            } else {
                _isOnline.value = false
                _refreshMessage.value = "Failed to update rates: using offline cache"
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

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.updateCurrenciesOrder(CurrenciesCatalog.defaultSelectedCodes)
            for (curr in CurrenciesCatalog.allCurrencies) {
                if (!CurrenciesCatalog.defaultSelectedCodes.contains(curr.code)) {
                    repository.toggleCurrencySelection(curr.code, false)
                }
            }
            setBgSyncEnabled(true)
            setBgSyncIntervalHours(4L)
            setAutoRefreshMinutes(3)
            _activeCurrencyCode.value = "USD"
            _activeHintAmount.value = "1.00"
            _activeInputText.value = ""
            _isHintActive.value = true
        }
    }
}
