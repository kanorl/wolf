package com.frost.io.netty

import io.netty.buffer.ByteBuf

fun ByteBuf.toArray(): ByteArray {
    if (hasArray()) {
        return array()
    }
    val array = ByteArray(readableBytes())
    readBytes(array)
    return array
}