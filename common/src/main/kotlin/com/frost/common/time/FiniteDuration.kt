package com.frost.common.time

import java.util.concurrent.TimeUnit

data class FiniteDuration(val value: Long, val timeUnit: TimeUnit) {
    companion object {
        val zero = 0.millis()
    }

    val millis = timeUnit.toMillis(value)
    val seconds = timeUnit.toSeconds(value)
    val minutes = timeUnit.toMinutes(value)
    val days = timeUnit.toDays(value)
    val nanos = timeUnit.toNanos(value)
    fun sleep() = timeUnit.sleep(value)
}

fun Int.millis(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.MILLISECONDS)
fun Int.seconds(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.SECONDS)
fun Int.minutes(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.MINUTES)
fun Int.days(): FiniteDuration = FiniteDuration(this.toLong(), TimeUnit.DAYS)


fun sleep(duration: FiniteDuration) = duration.timeUnit.sleep(duration.value)