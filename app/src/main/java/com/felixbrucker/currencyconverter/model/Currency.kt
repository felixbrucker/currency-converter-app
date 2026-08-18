package com.felixbrucker.currencyconverter.model

/**
 * Sealed interface to distinguish between Fiat and Crypto specific data.
 */
sealed interface CurrencyType {
    data class Fiat(
        val flagEmoji: String,
        val country: String
    ) : CurrencyType

    data class Crypto(
        val coinGeckoId: String,
        val imageUrl: String
    ) : CurrencyType
}

/**
 * Model representing a currency (Fiat or Crypto).
 */
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val type: CurrencyType,
    val decimalPlaces: Int
) {
    val isCrypto: Boolean get() = type is CurrencyType.Crypto

    val country: String? get() = (type as? CurrencyType.Fiat)?.country
}
