package com.felixbrucker.currencyconverter.extensions

import java.time.Instant

fun Instant.toKotlinInstant(): kotlin.time.Instant {
    return kotlin.time.Instant.fromEpochMilliseconds(this.toEpochMilli())
}
