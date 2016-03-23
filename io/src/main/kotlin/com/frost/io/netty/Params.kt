package com.frost.io.netty

import com.frost.common.lang.emptyByteArray
import com.frost.io.Param
import com.frost.io.Request
import com.frost.io.netty.handler.identity
import io.netty.channel.Channel
import io.netty.channel.ChannelId

class ChannelIdParam : Param<ChannelId> {
    override fun getValue(request: Request<*>, channel: Channel): ChannelId = channel.id()
}

class ChannelParam : Param<Channel> {
    override fun getValue(request: Request<*>, channel: Channel): Channel = channel
}

class IdentityParam : Param<Long> {
    override fun getValue(request: Request<*>, channel: Channel): Long = channel.identity() ?: -1
}

class RequestParam<T>(val type: Class<T>, val decoder: (ByteArray, Class<T>) -> T) : Param<T> {
    override fun getValue(request: Request<*>, channel: Channel): T = decoder(request.body as? ByteArray ?: emptyByteArray, type)
}