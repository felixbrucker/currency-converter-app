package com.felixbrucker.currencyconverter.data.remote.provider

import com.felixbrucker.currencyconverter.data.remote.CurrencyEnumType
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.data.remote.HttpApiProvider
import com.felixbrucker.currencyconverter.data.remote.LatestRatesResponse
import com.felixbrucker.currencyconverter.extensions.toKotlinInstant
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class Frankfurter(
    override val name: String = NAME,
    override val defaultEnabled: Boolean = false,
    override val updateFrequency: Duration = 1.days,
    override val supportedCurrencyTypes: Set<CurrencyEnumType> = setOf(CurrencyEnumType.Fiat),
): ExchangeRateProvider, HttpApiProvider() {
    companion object {
        const val NAME = "Frankfurter API"
    }
    @JsonClass(generateAdapter = true)
    data class RatesResponse(
        val date: String? = null,
        val base: String? = null,
        val quote: String? = null,
        val rate: Double? = null
    )
    interface ApiService {
        @GET("v2/rates")
        suspend fun getLatestRates(@Query("base") base: String = "USD"): List<RatesResponse>
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.dev/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    override suspend fun getLatestUsdRates(): LatestRatesResponse {
        val response = api.getLatestRates(base = "USD")

        val rates = response
            .associate { (it.quote ?: "") to (it.rate ?: 0.0) }
            .filter { it.key.isNotEmpty() }
        val updatedAtLocalDate = response
            .mapNotNull { entry -> entry.date?.let { LocalDate.parse(it) } }
            .maxOrNull()
        val updatedAt = if (updatedAtLocalDate != null) {
            // Rates are updated once daily at around 16:00 CET, use 16:30 to be safe
            val lastOrNextUpdateAt = updatedAtLocalDate.atTime(16, 30).atZone(ZoneId.of("CET")).toInstant().toKotlinInstant()
            if (lastOrNextUpdateAt < now()) {
                lastOrNextUpdateAt
            } else {
                lastOrNextUpdateAt.minus(1.days)
            }
        } else {
            Instant.DISTANT_PAST
        }


        return LatestRatesResponse(
            rates = rates,
            updatedAt = updatedAt,
            nextUpdateAt = updatedAt.plus(24.hours),
        )
    }
}
