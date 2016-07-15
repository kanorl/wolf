package com.frostwolf.io

data class Command(val module: Short, val cmd: Byte) {
    fun bytes(): ByteArray = byteArrayOf((module.toInt() shr 8).toByte(), (module.toInt() and 0xff).toByte(), cmd)
}

data class Request<out T>(val command: Command, val body: T?)

data class Response<out T>(val command: Command, val msg: T? = null, val code: Int)