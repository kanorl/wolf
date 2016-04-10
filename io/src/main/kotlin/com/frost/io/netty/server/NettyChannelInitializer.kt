package com.frost.io.netty.server

import com.frost.common.logging.getLogger
import com.frost.io.Compressor
import com.frost.io.netty.codec.RequestDecoder
import com.frost.io.netty.config.SocketSetting
import com.frost.io.netty.filter.ChannelFilter
import com.frost.io.netty.filter.RequestInterceptor
import com.frost.io.netty.handler.ChannelInboundTrafficController
import com.frost.io.netty.handler.ChannelManager
import com.frost.io.netty.handler.ChannelWriter
import com.frost.io.netty.handler.ServerHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class NettyChannelInitializer : ChannelInitializer<SocketChannel>() {
    private val logger by getLogger()

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
    @Autowired(required = false)
    private var compressor: Compressor? = null

    private val prepender = LengthFieldPrepender(4)
    private lateinit var filters: List<ChannelFilter>
    private lateinit var interceptors: List<RequestInterceptor>


    @PostConstruct
    private fun init() {
        filters = ctx.getBeansOfType(ChannelFilter::class.java).values.sorted()
        interceptors = ctx.getBeansOfType(RequestInterceptor::class.java).values.sorted()
    }

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        filters.forEach { pipeline.addLast(it) }
        pipeline.addLast("decoder", RequestDecoder(maxFrameLength = socketSetting.frameLengthMax, compressor = compressor))
                .addLast("lengthPrepender", prepender)
                .addLast("trafficController", ChannelInboundTrafficController(socketSetting.msgNumPerSecond))
                .addLast("channelManager", channelManager)
                .addLast("writer", writer)
                .addLast("readTimeoutHandler", ReadTimeoutHandler(socketSetting.readTimeoutSeconds))
        interceptors.forEach { pipeline.addLast(it) }
        pipeline.addLast("handler", handler)
    }
}