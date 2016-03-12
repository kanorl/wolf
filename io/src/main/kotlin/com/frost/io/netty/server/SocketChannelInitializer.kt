package com.frost.io.netty.server

import com.frost.common.concurrent.NamedThreadFactory
import com.frost.io.netty.codec.RequestDecoder
import com.frost.io.netty.config.SocketSetting
import com.frost.io.netty.filter.ChannelFilter
import com.frost.io.netty.handler.ChannelManager
import com.frost.io.netty.handler.ChannelTrafficController
import com.frost.io.netty.handler.ChannelWriter
import com.frost.io.netty.handler.ServerHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class SocketChannelInitializer : ChannelInitializer<SocketChannel>() {

    @Autowired
    private lateinit var socketSetting: SocketSetting
    @Autowired
    private lateinit var channelManager: ChannelManager
    @Autowired
    private lateinit var handler: ServerHandler
    @Autowired
    private lateinit var ctx: ApplicationContext
    @Autowired
    private lateinit var writer: ChannelWriter

    private val prepender = LengthFieldPrepender(4)
    private lateinit var executor: EventExecutorGroup
    private lateinit var filters: MutableMap<String, ChannelFilter>

    @PostConstruct
    private fun init() {
        executor = DefaultEventExecutorGroup(socketSetting.poolSize, NamedThreadFactory("io-handler"))
        filters = ctx.getBeansOfType(ChannelFilter::class.java)
    }

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        filters.forEach { pipeline.addLast(it.key, it.value) }
        pipeline.addLast("decoder", RequestDecoder(socketSetting.frameLengthMax, 0, 4, true))
                .addLast("encoder", prepender)
                .addLast("trafficController", ChannelTrafficController(socketSetting.msgNumPerSecond))
                .addLast("channelManager", channelManager)
                .addLast("writer", writer)
                .addLast("idleMonitor", IdleStateHandler(0, 0, socketSetting.idleSeconds))
                .addLast(executor, "handler", handler)
    }
}