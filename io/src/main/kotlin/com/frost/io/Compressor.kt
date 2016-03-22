package com.frost.io

interface Compressor {
    fun compress(bytes: ByteArray): ByteArray
}