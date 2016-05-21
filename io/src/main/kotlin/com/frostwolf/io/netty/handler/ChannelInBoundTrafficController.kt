package com.frostwolf.io.netty.handler

import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.time.currentMillis
import com.frostwolf.io.netty.ChannelInboundTrafficExcessException
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ChannelInboundTrafficController(val readLimit: Int) : ChannelInboundHandlerAdapter() {
    private val logger by getLogger()

    private var counter = 0
    private var lastCheckTime = 0L
    private var closeFuture: ChannelFuture? = null

    override fun channelActive(ctx: ChannelHandlerContext?) {
        lastCheckTime = currentMillis
        super.channelActive(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (closeFuture != null) {
            logger.debug("Channel closing due to traffic excess")
            return
        }
        val num = ++counter
        if (readLimit > 0 && num > readLimit) {
            val now = currentMillis
            if (now - lastCheckTime < 1000) {
                ctx.fireExceptionCaught(ChannelInboundTrafficExcessException("$num in ${now - lastCheckTime}ms"))
                closeFuture = ctx.close()
                return
            }
            counter = 0
            lastCheckTime = now
        }
        super.channelRead(ctx, msg)
    }
}