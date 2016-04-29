package com.frostwolf.io.netty.filter

import com.frostwolf.io.Request
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext

@ChannelHandler.Sharable
abstract class RequestInterceptor : ChannelInboundInterceptor<Request<ByteArray>>() {
    abstract override fun intercept(ctx: ChannelHandlerContext, msg: Request<ByteArray>): Boolean
}