package com.felixbrucker.currencyconverter.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Instant

interface ExchangeRateProvider {
    val name: String
    val requiresApiKey: Boolean
    val displayProperties: DisplayProperties
    suspend fun getLatestUsdRates(): LatestRatesResponse
}

data class DisplayProperties(
    val infoUrl: String,
    val supportedCurrencyTypes: Set<CurrencyEnumType>,
    val updateFrequency: Duration,
)

enum class CurrencyEnumType {
    Fiat,
    Crypto,
}

data class LatestRatesResponse(
    val rates: Map<String, Double>,
    val updatedAt: Instant,
    val nextUpdateAt: Instant,
)

open class HttpApiProvider {
    protected val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    protected val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()
}
