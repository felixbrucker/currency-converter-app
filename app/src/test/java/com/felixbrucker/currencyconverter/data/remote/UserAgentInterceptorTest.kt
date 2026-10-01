package com.felixbrucker.currencyconverter.data.remote

import com.felixbrucker.currencyconverter.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentInterceptorTest {

    @Test
    fun interceptorAddsUserAgentHeaderWithCustomValue() {
        val customUserAgent = "currency-converter/2.0.0"
        var interceptedRequest: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor(customUserAgent))
            .addInterceptor(Interceptor { chain ->
                interceptedRequest = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody(null))
                    .build()
            })
            .build()
        val request = Request.Builder()
            .url("https://example.com/api")
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals(customUserAgent, interceptedRequest?.header("User-Agent"))
    }

    @Test
    fun interceptorAddsUserAgentHeaderWithDefaultValue() {
        var interceptedRequest: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(Interceptor { chain ->
                interceptedRequest = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody(null))
                    .build()
            })
            .build()
        val request = Request.Builder()
            .url("https://example.com/api")
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        assertEquals("currency-converter/${BuildConfig.VERSION_NAME}", interceptedRequest?.header("User-Agent"))
    }
}
