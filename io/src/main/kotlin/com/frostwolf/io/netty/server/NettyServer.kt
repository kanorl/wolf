package com.frostwolf.io.netty.server

import com.frostwolf.common.concurrent.NamedThreadFactory
import com.frostwolf.common.logging.getLogger
import com.frostwolf.io.netty.config.SocketSetting
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFactory
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ApplicationContextEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextStartedEvent
import org.springframework.stereotype.Component
import java.lang.reflect.Field

@Component
open class NettyServer : ApplicationListener<ApplicationContextEvent> {
    val logger by getLogger()

    @Autowired
    private lateinit var socketSetting: SocketSetting
    @Autowired
    private lateinit var channelInitializer: ChannelInitializer<SocketChannel>

    private val parentGroup: EventLoopGroup = eventLoopGroup(1, NamedThreadFactory("socket-accept"))
    private val childGroup: EventLoopGroup = eventLoopGroup(0, NamedThreadFactory("socket-io"))

    private fun eventLoopGroup(nThread: Int = 0, factory: NamedThreadFactory): EventLoopGroup = if (Epoll.isAvailable()) EpollEventLoopGroup(nThread, factory) else NioEventLoopGroup(nThread, factory)

    override fun onApplicationEvent(event: ApplicationContextEvent) {
        when (event) {
            is ContextStartedEvent -> start()
            is ContextClosedEvent -> stop()
        }
    }

    @Synchronized
    private fun start() {
        logger.info("Socket server is starting.\n $socketSetting")
        try {
            val b = ServerBootstrap()
            b.group(parentGroup, childGroup)
                    .childHandler(channelInitializer)
                    .channelFactory(ChannelFactory<ServerSocketChannel> {
                        if (Epoll.isAvailable()) EpollServerSocketChannel() else NioServerSocketChannel()
                    })

            socketSetting.options.map { transfer(it) }.forEach { b.option(it.first, it.second) }
            socketSetting.childOptions.map { transfer(it) }.forEach { b.childOption(it.first, it.second) }

            b.bind(socketSetting.host, socketSetting.port).sync()
        } catch(e: Exception) {
            logger.error("Socket server starts failed", e)
            throw e
        }
        logger.info("Socket server started, listening on ${socketSetting.host}:${socketSetting.port}")
    }

    @Suppress("UNCHECKED_CAST")
    private val transfer: (e: Map.Entry<String, Any>) -> Pair<ChannelOption<Any>, Any> = {
        val field: Field
        try {
            field = ChannelOption::class.java.getField(it.key)
        } catch(ex: NoSuchFieldException) {
            throw IllegalArgumentException("Unsupported socket option [${it.key}]")
        }
        val key = field.get(null) as ChannelOption<Any>
        key to it.value
    }

    @Synchronized
    private fun stop() {
        parentGroup.shutdownGracefully().sync()
        childGroup.shutdownGracefully().sync()
        logger.info("Socket server closed.....")
    }
}