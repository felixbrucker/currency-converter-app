package com.felixbrucker.currencyconverter.extensions

import retrofit2.HttpException

fun Throwable.toStringExtended(): String {
    if (this is HttpException) {
        return "HttpException(code=${code()}, message=$message, body=${response()?.errorBody()?.string()})"
    }
    return toString()
}

fun Map<String, Throwable>.aggregate(): Throwable {
    return Exception(entries.joinToString("; ") { "${it.key}: ${it.value.toStringExtended()}" })
}
