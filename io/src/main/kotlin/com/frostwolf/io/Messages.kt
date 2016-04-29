package com.frostwolf.io

import java.nio.ByteBuffer

data class Command(val module: Short, val cmd: Byte){
    fun bytes(): ByteArray = ByteBuffer.allocate(3).putShort(module).put(cmd).array()
}

data class Request<T>(val command: Command, val body: T?)

data class Response<T>(val command: Command, val msg: T? = null, val code: Int)