package com.felixbrucker.currencyconverter.data.remote

import com.felixbrucker.currencyconverter.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(
    private val userAgent: String = "currency-converter/${BuildConfig.VERSION_NAME}"
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}
