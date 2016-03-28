package com.frost.io.netty.filter

import com.frost.io.Request
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext

@ChannelHandler.Sharable
abstract class RequestInterceptor : ChannelInboundInterceptor<Request<ByteArray>>() {
    abstract override fun intercept(ctx: ChannelHandlerContext, msg: Request<ByteArray>): Boolean
}