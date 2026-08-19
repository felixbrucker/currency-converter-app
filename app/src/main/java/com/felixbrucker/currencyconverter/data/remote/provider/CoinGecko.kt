package com.felixbrucker.currencyconverter.data.remote.provider

import com.felixbrucker.currencyconverter.data.CurrenciesCatalog
import com.felixbrucker.currencyconverter.data.remote.CurrencyEnumType
import com.felixbrucker.currencyconverter.data.remote.DisplayProperties
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.data.remote.HttpApiProvider
import com.felixbrucker.currencyconverter.data.remote.LatestRatesResponse
import com.felixbrucker.currencyconverter.model.CurrencyType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.collections.chunked
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.joinToString
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class CoinGecko(
    override val name: String = NAME,
    override val defaultEnabled: Boolean = true,
    override val displayProperties: DisplayProperties = DisplayProperties(
        infoUrl = "https://www.coingecko.com/en/api",
        supportedCurrencyTypes = setOf(CurrencyEnumType.Crypto),
        updateFrequency = 5.minutes,
    ),
): ExchangeRateProvider, HttpApiProvider() {
    companion object {
        const val NAME = "CoinGecko API"
    }
    @JsonClass(generateAdapter = true)
    data class SimpleUsdPriceResult(
        // To simplify typing we hardcode usd here, this actually depends on the vs_currencies
        // passed to the api
        val usd: Double,
        @Json(name = "last_updated_at")
        val lastUpdatedAtUnix: Long,
    )

    interface ApiService {
        @GET("simple/price")
        suspend fun getPrices(
            @Query("ids") ids: String,
            @Query("vs_currencies") vsCurrencies: String = "usd",
            @Query("include_last_updated_at") includeLastUpdatedAt: Boolean = true,
        ): Map<String, SimpleUsdPriceResult>
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.coingecko.com/api/v3/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    override suspend fun getLatestUsdRates(): LatestRatesResponse {
        val coinGeckoIdsToCode = mutableMapOf<String, String>()
        val coinGeckoIds = CurrenciesCatalog.allCurrencies.mapNotNull {
            when (it.type) {
                is CurrencyType.Crypto -> {
                    coinGeckoIdsToCode[it.type.coinGeckoId] = it.code

                    it.type.coinGeckoId
                }
                else -> null
            }
        }

        val allRates = mutableMapOf<String, Double>()
        var latestUpdatedAt = Instant.DISTANT_PAST
        // CoinGecko allows up to 500 coins per request for simple/price
        coinGeckoIds.chunked(500).forEach { chunk ->
            val idsString = chunk.joinToString(",")
            val response = api.getPrices(ids = idsString, vsCurrencies = "usd")
            response.forEach { (id, priceResult) ->
                val usdPrice = priceResult.usd
                val code = coinGeckoIdsToCode[id] ?: return@forEach
                allRates[code.uppercase()] = 1.0 / usdPrice
            }
            val updatedAt = response
                .mapNotNull { (_, priceResult) ->
                    Instant.fromEpochSeconds(priceResult.lastUpdatedAtUnix)
                }
                .maxOrNull() ?: Instant.DISTANT_PAST
            if (latestUpdatedAt < updatedAt) {
                latestUpdatedAt = updatedAt
            }
        }

        return LatestRatesResponse(
            rates = allRates,
            updatedAt = latestUpdatedAt,
            nextUpdateAt = latestUpdatedAt.plus(5.minutes),
        )
    }
}
