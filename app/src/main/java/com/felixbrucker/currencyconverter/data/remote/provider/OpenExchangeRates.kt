package com.felixbrucker.currencyconverter.data.remote.provider

import com.felixbrucker.currencyconverter.data.remote.CurrencyEnumType
import com.felixbrucker.currencyconverter.data.remote.DisplayProperties
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.data.remote.HttpApiProvider
import com.felixbrucker.currencyconverter.data.remote.LatestRatesResponse
import com.felixbrucker.currencyconverter.data.local.ExchangeRateProviderEntity
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class OpenExchangeRates(
    private val configFlow: Flow<ExchangeRateProviderEntity>,
    override val name: String = NAME,
    override val requiresApiKey: Boolean = true,
    override val displayProperties: DisplayProperties = DisplayProperties(
        infoUrl = "https://openexchangerates.org",
        supportedCurrencyTypes = setOf(CurrencyEnumType.Fiat),
        updateFrequency = 1.hours,
    ),
): ExchangeRateProvider, HttpApiProvider() {
    companion object {
        const val NAME = "Open Exchange Rates"
        val DEFAULT_ENTITY = ExchangeRateProviderEntity(
            name = NAME,
            isEnabled = false
        )
    }

    @JsonClass(generateAdapter = true)
    data class RatesResponse(
        val timestamp: Long,
        val base: String,
        val rates: Map<String, Double> = emptyMap()
    )

    interface ApiService {
        @GET("latest.json")
        suspend fun getLatestRates(
            @Header("Authorization") auth: String
        ): RatesResponse
    }

    private val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://openexchangerates.org/api/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    override suspend fun getLatestUsdRates(): LatestRatesResponse {
        val config = configFlow.first()
        val apiKey = config.apiKey
        if (apiKey.isNullOrBlank()) {
            throw Exception("API Key (App ID) is missing for Open Exchange Rates")
        }

        val response = api.getLatestRates(auth = "Token $apiKey")
        val updatedAt = Instant.fromEpochSeconds(response.timestamp)

        return LatestRatesResponse(
            rates = response.rates,
            updatedAt = updatedAt,
            nextUpdateAt = updatedAt.plus(1.hours),
        )
    }
}
