package com.frostwolf.io.netty.filter

import com.frostwolf.common.Ordered
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.internal.TypeParameterMatcher


abstract class ChannelInboundInterceptor<in I> : ChannelInboundHandlerAdapter(), Ordered {
    private val matcher = TypeParameterMatcher.find(this, ChannelInboundInterceptor::class.java, "I")

    @Suppress("UNCHECKED_CAST")
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (!matcher.match(msg) || !intercept(ctx, msg as I)) {
            super.channelRead(ctx, msg)
        }
    }

    abstract fun intercept(ctx: ChannelHandlerContext, msg: I): Boolean
}