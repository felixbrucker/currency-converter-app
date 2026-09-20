package com.felixbrucker.currencyconverter.util

import com.felixbrucker.currencyconverter.model.Currency
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
        groupingSeparator = ','
    }

    // Thread-local cache of DecimalFormat instances to avoid heavy allocations during rapid UI updates
    private val threadLocalFormatters = ThreadLocal.withInitial { mutableMapOf<String, DecimalFormat>() }

    // Pre-allocated pattern strings for crypto precision formatting
    private val cryptoPatterns = Array(11) { p ->
        "0." + "0".repeat(p.coerceAtLeast(1))
    }

    private fun getDecimalFormat(pattern: String): DecimalFormat {
        val map = threadLocalFormatters.get() ?: mutableMapOf<String, DecimalFormat>().also { threadLocalFormatters.set(it) }
        return map.getOrPut(pattern) {
            DecimalFormat(pattern, symbols)
        }
    }

    fun formatAmount(amount: Double, currency: Currency): String {
        if (amount.isNaN() || amount.isInfinite()) return "0"
        return when {
            currency.isCrypto -> {
                val precision = currency.decimalPlaces
                when {
                    amount >= 1000 -> getDecimalFormat("#,##0.00").format(amount)
                    amount >= 1 -> getDecimalFormat("#,##0.0000").format(amount)
                    amount >= 0.0001 -> {
                        val p = precision.coerceIn(2, 8)
                        getDecimalFormat(cryptoPatterns[p]).format(amount)
                    }
                    amount > 0 -> {
                        val p = precision.coerceIn(2, 10)
                        getDecimalFormat(cryptoPatterns[p]).format(amount)
                    }
                    else -> "0.00"
                }
            }
            currency.decimalPlaces == 0 -> {
                getDecimalFormat("#,##0").format(amount)
            }
            currency.decimalPlaces == 3 -> {
                getDecimalFormat("#,##0.000").format(amount)
            }
            else -> {
                getDecimalFormat("#,##0.00").format(amount)
            }
        }
    }

    fun formatRate(rate: Double, targetCurrency: Currency): String {
        if (rate.isNaN() || rate.isInfinite() || rate == 0.0) return "0.00"
        return when {
            rate >= 1000 -> getDecimalFormat("#,##0.00").format(rate)
            rate >= 1 -> getDecimalFormat("0.0000").format(rate)
            rate >= 0.0001 -> getDecimalFormat("0.000000").format(rate)
            else -> getDecimalFormat("0.00000000").format(rate)
        }
    }

    fun cleanInput(input: String): String {
        // Keep digits, math operators and at most one decimal point (unless in math expression)
        val cleaned = StringBuilder()
        val mathOperators = setOf('+', '-', '*', '/', '(', ')', '×', '÷')
        var hasDot = false
        val isMath = input.any { it in mathOperators }

        for (char in input) {
            if (char.isDigit()) {
                cleaned.append(char)
            } else if (char == '.' || char == ',') {
                if (isMath) {
                    cleaned.append('.') // In math, we might have multiple dots (e.g. 1.5 + 2.5)
                } else if (!hasDot) {
                    cleaned.append('.')
                    hasDot = true
                }
            } else if (char in mathOperators) {
                cleaned.append(char)
            }
        }
        return cleaned.toString()
    }

}
