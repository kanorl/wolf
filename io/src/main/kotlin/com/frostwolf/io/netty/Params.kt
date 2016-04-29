package com.frostwolf.io.netty

import com.frostwolf.common.lang.emptyByteArray
import com.frostwolf.io.Request
import com.frostwolf.io.netty.handler.identity
import io.netty.channel.Channel
import io.netty.channel.ChannelId

interface Param<T> {
    fun getValue(request: Request<*>, channel: Channel): T;
}

class ChannelIdParam : Param<ChannelId> {
    override fun getValue(request: Request<*>, channel: Channel): ChannelId = channel.id()
}

class ChannelParam : Param<Channel> {
    override fun getValue(request: Request<*>, channel: Channel): Channel = channel
}

class IdentityParam : Param<Long> {
    override fun getValue(request: Request<*>, channel: Channel): Long = channel.identity()?.value ?: -1
}

class RequestParam<T>(val type: Class<T>, val decoder: (ByteArray, Class<T>) -> T) : Param<T> {
    override fun getValue(request: Request<*>, channel: Channel): T = decoder(request.body as? ByteArray ?: emptyByteArray, type)
}