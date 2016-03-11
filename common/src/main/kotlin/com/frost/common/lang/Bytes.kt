package com.frost.common.lang

import com.frost.common.lang.BitConverter.*
import com.google.common.primitives.Bytes

val emptyByteArray = byteArrayOf()

fun Short.bytes(): ByteArray = getBytes(this)

fun ByteArray.toShort(): Short = toShort(this)

fun Int.bytes(): ByteArray = getBytes(this)

fun ByteArray.toInt(): Int = toInt(this)

fun Long.bytes(): ByteArray = getBytes(this)

fun ByteArray.toLong(): Long = toLong(this)

fun combine(vararg arrays: ByteArray): ByteArray = arrays.flatMap { array -> Bytes.asList(*array) }.toByteArray()