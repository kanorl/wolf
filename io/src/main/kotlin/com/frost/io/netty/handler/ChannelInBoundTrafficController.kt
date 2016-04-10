package com.frost.io.netty.handler

import com.frost.common.logging.getLogger
import com.frost.io.netty.ChannelInboundTrafficExcessException
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.util.concurrent.atomic.AtomicInteger

class ChannelInboundTrafficController(val readLimit: Int) : ChannelInboundHandlerAdapter() {
    private val logger by getLogger()

    private val counter = AtomicInteger()
    private var lastCheckTime = 0L
    private var closeFuture: ChannelFuture? = null

    override fun channelActive(ctx: ChannelHandlerContext?) {
        lastCheckTime = System.currentTimeMillis()
        super.channelActive(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (closeFuture != null) {
            logger.debug("Channel closing for traffic excess")
            return
        }
        val num = counter.incrementAndGet()
        if (readLimit > 0 && num > readLimit) {
            val now = System.currentTimeMillis()
            if (now - lastCheckTime < 1000) {
                ctx.fireExceptionCaught(ChannelInboundTrafficExcessException)
                closeFuture = ctx.close()
            }
            counter.set(0)
            lastCheckTime = now
        }
        super.channelRead(ctx, msg)
    }
}