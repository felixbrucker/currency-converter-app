package com.felixbrucker.currencyconverter.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_providers")
data class CurrencyProviderEntity(
    @PrimaryKey
    val name: String,
    val isEnabled: Boolean,
    val displayOrder: Int,
    val lastSyncTimestamp: Long? = null
)
