package com.felixbrucker.currencyconverter.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM exchange_rates")
    fun getAllRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE code = :code")
    suspend fun getRateByCode(code: String): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)

    @Query("SELECT * FROM user_currencies ORDER BY displayOrder ASC")
    fun getUserCurrencies(): Flow<List<UserCurrencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCurrencies(currencies: List<UserCurrencyEntity>)

    @Query("UPDATE user_currencies SET isSelected = :isSelected WHERE code = :code")
    suspend fun toggleCurrencySelection(code: String, isSelected: Boolean)

    @Query("DELETE FROM user_currencies WHERE code = :code")
    suspend fun deleteUserCurrency(code: String)

    @Transaction
    suspend fun replaceUserCurrencies(currencies: List<UserCurrencyEntity>) {
        clearUserCurrencies()
        insertUserCurrencies(currencies)
    }

    @Query("DELETE FROM user_currencies")
    suspend fun clearUserCurrencies()

    @Query("SELECT * FROM app_settings WHERE key = :key")
    fun getSettingFlow(key: String): Flow<AppSettingEntity?>

    @Query("SELECT * FROM app_settings WHERE key = :key")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
