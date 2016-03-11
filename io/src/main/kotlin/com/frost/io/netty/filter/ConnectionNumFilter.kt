package com.frost.io.netty.filter

import com.frost.io.netty.config.SocketSetting
import io.netty.channel.ChannelHandlerContext
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.atomic.AtomicInteger

abstract class ConnectionNumFilter : ChannelFilter() {

    @Autowired
    private lateinit var socketSetting: SocketSetting

    private val num = AtomicInteger()

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
        num.incrementAndGet()
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        num.incrementAndGet()
    }

    override fun accept(ctx: ChannelHandlerContext): Boolean {
        // TODO waiting queue
        return num.get() < socketSetting.connectionsMax
    }
}