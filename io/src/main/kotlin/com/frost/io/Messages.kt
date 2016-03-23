package com.frost.io

import com.frost.common.lang.bytes
import com.frost.common.lang.toShort
import io.netty.channel.Channel
import java.util.*

final class Command {
    val bytes: ByteArray

    constructor(module: Short, cmd: Byte) {
        bytes = ByteArray(3)
        val mBytes = module.bytes()
        bytes[0] = mBytes[0]
        bytes[1] = mBytes[1]
        bytes[2] = cmd
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(bytes)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is Command) {
            return false
        }
        return Arrays.equals(bytes, other.bytes)
    }

    override fun toString(): String {
        return "module=${bytes.sliceArray(0..1).toShort()}, cmd=${bytes[2]}"
    }
}

data class Request<T>(val command: Command, val body: T?, val channel: Channel)

data class Response<T>(val command: Command, val msg: T? = null, val code: Int)