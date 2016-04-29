package com.frostwolf.io.netty.filter

import com.frostwolf.common.Ordered
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext

interface ChannelFilter : Ordered {
    fun accept(ctx: ChannelHandlerContext): Boolean

    fun channelRejected(ctx: ChannelHandlerContext): ChannelFuture? = null
}