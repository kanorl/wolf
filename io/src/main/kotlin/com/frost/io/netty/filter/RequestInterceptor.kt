package com.frost.io.netty.filter

import com.frost.common.Ordered
import com.frost.io.Request
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

@ChannelHandler.Sharable
abstract class RequestInterceptor : SimpleChannelInboundHandler<Request<ByteArray>>(), Ordered {

    override final fun channelRead0(ctx: ChannelHandlerContext, msg: Request<ByteArray>) {
        if (!intercept(ctx, msg)) {
            ctx.fireChannelRead(msg)
        }
    }

    abstract fun intercept(ctx: ChannelHandlerContext, msg: Request<ByteArray>): Boolean
}