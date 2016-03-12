package com.frost.io.netty

import com.frost.io.netty.handler.identity
import io.netty.channel.Channel
import io.netty.channel.ChannelId

interface Param<T> {
    fun getValue(request: Request): T;
}

class ChannelIdParam : Param<ChannelId> {
    override fun getValue(request: Request): ChannelId = request.channel.id()
}

class ChannelParam : Param<Channel> {
    override fun getValue(request: Request): Channel = request.channel
}

class IdentityParam : Param<Long> {
    override fun getValue(request: Request): Long = request.channel.identity() ?: -1
}

class RequestParam<T>(val type: Class<T>, val decoder: (ByteArray, Class<T>) -> T) : Param<T> {
    override fun getValue(request: Request): T = decoder(request.body, type)
}