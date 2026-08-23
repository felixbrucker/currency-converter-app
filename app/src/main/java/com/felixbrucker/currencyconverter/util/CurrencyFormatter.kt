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

    fun formatAmount(amount: Double, currency: Currency): String {
        if (amount.isNaN() || amount.isInfinite()) return "0"
        return when {
            currency.isCrypto -> {
                val precision = currency.decimalPlaces
                when {
                    amount >= 1000 -> DecimalFormat("#,##0.00", symbols).format(amount)
                    amount >= 1 -> DecimalFormat("#,##0.0000", symbols).format(amount)
                    amount >= 0.0001 -> {
                        val pattern = "0." + "0".repeat(precision.coerceIn(2, 8))
                        DecimalFormat(pattern, symbols).format(amount)
                    }
                    amount > 0 -> {
                        val pattern = "0." + "0".repeat(precision.coerceIn(2, 10))
                        DecimalFormat(pattern, symbols).format(amount)
                    }
                    else -> "0.00"
                }
            }
            currency.decimalPlaces == 0 -> {
                DecimalFormat("#,##0", symbols).format(amount)
            }
            currency.decimalPlaces == 3 -> {
                DecimalFormat("#,##0.000", symbols).format(amount)
            }
            else -> {
                DecimalFormat("#,##0.00", symbols).format(amount)
            }
        }
    }

    fun formatRate(rate: Double, targetCurrency: Currency): String {
        if (rate.isNaN() || rate.isInfinite() || rate == 0.0) return "0.00"
        return when {
            rate >= 1000 -> DecimalFormat("#,##0.00", symbols).format(rate)
            rate >= 1 -> DecimalFormat("0.0000", symbols).format(rate)
            rate >= 0.0001 -> DecimalFormat("0.000000", symbols).format(rate)
            else -> DecimalFormat("0.00000000", symbols).format(rate)
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
