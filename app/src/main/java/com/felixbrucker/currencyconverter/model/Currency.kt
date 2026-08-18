package com.felixbrucker.currencyconverter.model

/**
 * Model representing a currency (Fiat or Crypto).
 */
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String,
    val isCrypto: Boolean = false,
    val country: String = "",
    val decimalPlaces: Int = if (isCrypto) 6 else 2,
    val isPopular: Boolean = false
)
