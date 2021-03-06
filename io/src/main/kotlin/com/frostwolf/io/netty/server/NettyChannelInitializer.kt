package com.frostwolf.io.netty.server

import com.frostwolf.common.logging.getLogger
import com.frostwolf.io.Compressor
import com.frostwolf.io.netty.codec.RequestDecoder
import com.frostwolf.io.netty.config.SocketSetting
import com.frostwolf.io.netty.filter.ChannelFilterManager
import com.frostwolf.io.netty.filter.RequestInterceptor
import com.frostwolf.io.netty.handler.ChannelInboundTrafficController
import com.frostwolf.io.netty.handler.ChannelManager
import com.frostwolf.io.netty.handler.ChannelWriter
import com.frostwolf.io.netty.handler.ServerHandler
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
    @Autowired
    private lateinit var channelFilterManager: ChannelFilterManager
    @Autowired(required = false)
    private var compressor: Compressor? = null

    private val prepender = LengthFieldPrepender(4)
    private lateinit var interceptors: List<RequestInterceptor>


    @PostConstruct
    private fun init() {
        interceptors = ctx.getBeansOfType(RequestInterceptor::class.java).values.sorted()
    }

    override fun initChannel(ch: SocketChannel) {
        val pipeline = ch.pipeline()
        pipeline.addLast("channelFilterManager", channelFilterManager)
                .addLast("decoder", RequestDecoder(maxFrameLength = socketSetting.frameLengthMax.value, lengthFieldLength = socketSetting.lengthFieldLength, compressor = compressor))
                .addLast("lengthPrepender", prepender)
                .addLast("trafficController", ChannelInboundTrafficController(socketSetting.msgNumPerSecond))
                .addLast("channelManager", channelManager)
                .addLast("writer", writer)
                .addLast("readTimeoutHandler", ReadTimeoutHandler(socketSetting.readTimeout.seconds.toInt()))
        interceptors.forEach { pipeline.addLast(it) }
        pipeline.addLast("handler", handler)
    }
}