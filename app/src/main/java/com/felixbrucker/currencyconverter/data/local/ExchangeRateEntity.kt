package com.felixbrucker.currencyconverter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val code: String,
    val rateToUsd: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
