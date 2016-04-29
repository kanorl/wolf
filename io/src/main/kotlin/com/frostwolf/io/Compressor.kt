package com.frostwolf.io

interface Compressor {
    fun compress(bytes: ByteArray): ByteArray
    fun decompress(bytes: ByteArray): ByteArray
}