package com.felixbrucker.currencyconverter.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenErApiResponse(
    val result: String? = null,
    val provider: String? = null,
    @Json(name = "time_last_update_unix")
    val timeLastUpdateUnix: Long? = null,
    @Json(name = "time_next_update_unix")
    val timeNextUpdateUnix: Long? = null,
    @Json(name = "base_code")
    val baseCode: String? = null,
    val rates: Map<String, Double> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class FrankfurterResponse(
    val amount: Double? = null,
    val base: String? = null,
    val date: String? = null,
    val rates: Map<String, Double> = emptyMap()
)

interface OpenErApiService {
    @GET("v6/latest/USD")
    suspend fun getLatestRates(): OpenErApiResponse
}

interface FrankfurterApiService {
    @GET("v1/latest")
    suspend fun getLatestRates(@Query("base") base: String = "USD"): FrankfurterResponse
}

object NetworkClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val openErApi: OpenErApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://open.er-api.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenErApiService::class.java)
    }

    val frankfurterApi: FrankfurterApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.frankfurter.dev/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FrankfurterApiService::class.java)
    }
}
