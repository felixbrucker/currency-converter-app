package com.felixbrucker.currencyconverter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "exchange_rate_providers")
data class ExchangeRateProviderEntity(
    @PrimaryKey
    val name: String,
    val isEnabled: Boolean,
    val lastUpdatedAt: Instant? = null,
    val nextUpdateAt: Instant? = null,
    val apiKey: String? = null,
)
