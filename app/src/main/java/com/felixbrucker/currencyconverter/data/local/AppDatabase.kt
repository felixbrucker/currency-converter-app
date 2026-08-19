package com.felixbrucker.currencyconverter.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import kotlin.time.Instant

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }
}

@Database(
    entities = [
        ExchangeRateEntity::class,
        UserCurrencyEntity::class,
        AppSettingEntity::class,
        ExchangeRateProviderEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = MigrationSpec2To3::class),
        AutoMigration(from = 3, to = 4),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "currency_converter.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@RenameColumn(
    tableName = "exchange_rates",
    fromColumnName = "lastUpdated",
    toColumnName = "lastUpdatedAt"
)
@RenameTable(
    fromTableName = "currency_providers",
    toTableName = "exchange_rate_providers"
)
@DeleteColumn(
    tableName = "currency_providers",
    columnName = "displayOrder"
)
@RenameColumn(
    tableName = "currency_providers",
    fromColumnName = "lastSyncTimestamp",
    toColumnName = "lastUpdatedAt"
)
internal class MigrationSpec2To3 : AutoMigrationSpec
