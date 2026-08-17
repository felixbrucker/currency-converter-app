package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_currencies")
data class UserCurrencyEntity(
    @PrimaryKey
    val code: String,
    val displayOrder: Int,
    val isSelected: Boolean = true
)
