package com.felixbrucker.currencyconverter.data.remote.provider

import com.felixbrucker.currencyconverter.data.local.ExchangeRateProviderEntity
import com.felixbrucker.currencyconverter.data.remote.CurrencyEnumType
import com.felixbrucker.currencyconverter.data.remote.DisplayProperties
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.data.remote.HttpApiProvider
import com.felixbrucker.currencyconverter.data.remote.LatestRatesResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class ExchangeRateApi(
    override val name: String = NAME,
    override val requiresApiKey: Boolean = false,
    override val displayProperties: DisplayProperties = DisplayProperties(
        infoUrl = "https://www.exchangerate-api.com",
        supportedCurrencyTypes = setOf(CurrencyEnumType.Fiat),
        updateFrequency = 1.days,
    ),
): ExchangeRateProvider, HttpApiProvider() {
    companion object {
        const val NAME = "ExchangeRate-API"
        val DEFAULT_ENTITY = ExchangeRateProviderEntity(
            name = NAME,
            isEnabled = true
        )
    }
    @JsonClass(generateAdapter = true)
    data class RatesResponse(
        val result: String? = null,
        @Json(name = "time_last_update_unix")
        val timeLastUpdateUnix: Long? = null,
        @Json(name = "time_next_update_unix")
        val timeNextUpdateUnix: Long? = null,
        val rates: Map<String, Double> = emptyMap()
    )
    interface ApiService {
        @GET("v6/latest/USD")
        suspend fun getLatestUsdRates(): RatesResponse
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    override suspend fun getLatestUsdRates(): LatestRatesResponse {
        val response = api.getLatestUsdRates()

        return LatestRatesResponse(
            rates = response.rates,
            updatedAt = Instant.fromEpochSeconds(response.timeLastUpdateUnix ?: 0L),
            nextUpdateAt = Instant.fromEpochSeconds(response.timeNextUpdateUnix ?: 0L),
        )
    }
}
