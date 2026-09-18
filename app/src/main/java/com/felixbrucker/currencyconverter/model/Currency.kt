package com.felixbrucker.currencyconverter.model

import androidx.compose.runtime.Immutable

/**
 * Sealed interface to distinguish between Fiat and Crypto specific data.
 */
@Immutable
sealed interface CurrencyType {
    @Immutable
    data class Fiat(
        val flagEmoji: String,
        val country: String
    ) : CurrencyType

    @Immutable
    data class Crypto(
        val coinGeckoId: String,
        val imageUrl: String
    ) : CurrencyType
}

/**
 * Model representing a currency (Fiat or Crypto).
 */
@Immutable
data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val type: CurrencyType,
    val decimalPlaces: Int
) {
    val isCrypto: Boolean get() = type is CurrencyType.Crypto

    val country: String? get() = (type as? CurrencyType.Fiat)?.country

    val lowerCode: String = code.lowercase()
    val lowerName: String = name.lowercase()
    val lowerTypeKey: String = when (type) {
        is CurrencyType.Fiat -> type.country.lowercase()
        is CurrencyType.Crypto -> type.coinGeckoId.lowercase()
    }
}
