package com.frostwolf.common.lang

val emptyByteArray = byteArrayOf()

fun Short.toByteArray(): ByteArray {
    val i = this.toInt()
    val bytes = ByteArray(2)
    bytes[0] = ((i shr 8) and 0xff).toByte()
    bytes[1] = (i and 0xff).toByte()
    return bytes
}

fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[0] = ((this shr  24) and 0xff).toByte()
    bytes[1] = ((this shr 16) and 0xff).toByte()
    bytes[2] = ((this shr 8) and 0xff).toByte()
    bytes[3] = (this and 0xff).toByte()
    return bytes
}

fun Long.toByteArray(): ByteArray {
    val bytes = ByteArray(8)
    bytes[0] = ((this shr 56) and 0xff).toByte()
    bytes[1] = ((this shr 48) and 0xff).toByte()
    bytes[2] = ((this shr 40) and 0xff).toByte()
    bytes[3] = ((this shr 32) and 0xff).toByte()
    bytes[4] = ((this shr 24) and 0xff).toByte()
    bytes[5] = ((this shr 16) and 0xff).toByte()
    bytes[6] = ((this shr 8) and 0xff).toByte()
    bytes[7] = (this and 0xff).toByte()
    return bytes
}

fun ByteArray.toShort(): Short = (((this[0].toInt() and 0xff) shl 8) or (this[1].toInt() and 0xff)).toShort()

fun ByteArray.toInt(): Int = ((this[0].toInt() and 0xff) shl 24) or ((this[1].toInt() and 0xff) shl 16) or ((this[2].toInt() and 0xff) shl 8) or (this[3].toInt() and 0xff)

fun ByteArray.toLong(): Long = (this[0].toLong() and 0xff) shl 56 or ((this[1].toLong() and 0xff) shl 48) or ((this[2].toLong() and 0xff) shl 40) or ((this[3].toLong() and 0xff) shl 32) or ((this[4].toLong() and 0xff) shl 24) or ((this[5].toLong() and 0xff) shl 16) or ((this[6].toLong() and 0xff) shl 8) or (this[7].toLong() and 0xff)

fun Boolean.toInt(): Int = if (this) 1 else 0