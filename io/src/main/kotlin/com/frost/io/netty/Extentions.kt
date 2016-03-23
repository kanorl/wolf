package com.frost.io.netty

import io.netty.buffer.ByteBuf

fun ByteBuf.toArray(): ByteArray {
    val array = ByteArray(readableBytes())
    readBytes(array)
    return array
}