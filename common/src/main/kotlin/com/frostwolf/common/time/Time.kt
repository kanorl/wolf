package com.frostwolf.common.time

import java.time.*
import java.util.*

fun LocalDateTime.toDate(): Date = Date.from(this.atZone(ZoneId.systemDefault()).toInstant())

fun LocalDateTime.toMillis(): Long = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun Date.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())

val currentMillis: Long
    get() = System.currentTimeMillis()

@Suppress("IMPLICIT_CAST_TO_ANY")
inline fun <reified T : Any> now(): T {
    return when (T::class.java) {
        Long::class.javaObjectType -> System.currentTimeMillis()
        LocalDateTime::class.java -> LocalDateTime.now()
        LocalDate::class.java -> LocalDate.now()
        LocalTime::class.java -> LocalTime.now()
        Date::class.java -> Date()
        else -> throw UnsupportedOperationException()
    }  as T
}