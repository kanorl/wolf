package com.frostwolf.io.netty.client

import com.frostwolf.common.logging.getLogger
import com.frostwolf.common.time.currentMillis
import com.frostwolf.io.Command
import com.frostwolf.io.Request
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class NettyClient {
    private val logger by getLogger()

    private val bootstrap: Bootstrap = Bootstrap()
    private val eventLoopGroup: EventLoopGroup
    private val channelInitializer: ChannelInitializer<SocketChannel>
    private var channel: Channel? = null
    private val socketAddress: InetSocketAddress
    private val reconnectInterval = 30000
    private var lastConnectTime = 0L

    constructor(host: String = "127.0.0.1", port: Int, eventLoopGroup: EventLoopGroup? = null, channelInitializer: ChannelInitializer<SocketChannel> = ClientChannelInitializer(), options: Map<ChannelOption<Any>, Any> = mapOf()) {
        this.eventLoopGroup = eventLoopGroup ?: if (Epoll.isAvailable()) EpollEventLoopGroup() else NioEventLoopGroup()
        this.channelInitializer = channelInitializer
        this.socketAddress = InetSocketAddress(host, port)
        bootstrap.group(this.eventLoopGroup).channel(if (Epoll.isAvailable()) EpollSocketChannel::class.java else NioSocketChannel::class.java).handler(channelInitializer)
        for ((k, v) in options) {
            bootstrap.option(k, v)
        }
    }

    fun connect() {
        if (channel?.isActive ?: false) {
            return
        }
        synchronized(this) {
            if (currentMillis - lastConnectTime < reconnectInterval) {
                return;
            }
            lastConnectTime = currentMillis
        }

        try {
            channel = null
            val future = bootstrap.connect(socketAddress)
            if (!future.sync().await(10000)) {
                return;
            }
            channel = future.channel()
        } catch(e: Throwable) {
            logger.error("连接失败: ${e.message}", e)
        }
    }

    fun close() {
        eventLoopGroup.shutdownGracefully().sync()
    }

    fun write(cmd: Command, msg: Any? = null) {
        connect()
        channel?.writeAndFlush(Request(cmd, msg))
    }

    fun scheduleWithFixedDelay(delay: Long, task: () -> Unit) {
        connect()
        channel?.eventLoop()?.scheduleWithFixedDelay(task, 0, delay, TimeUnit.MILLISECONDS)
    }

    fun scheduleAtFixedRate(period: Long, task: () -> Unit) {
        connect()
        channel?.eventLoop()?.scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS)
    }

    fun scheduleOnce(delay: Long, task: () -> Unit) {
        connect()
        channel?.eventLoop()?.schedule(task, delay, TimeUnit.MILLISECONDS)
    }
}

fun main(args: Array<String>) {
    val a = AtomicInteger()
    val eventLoopGroup = NioEventLoopGroup(8)
//    val client = NettyClient(port = 5555, eventLoopGroup = eventLoopGroup)
//    client.connect()
        for (i in 1..10) {
            val client = NettyClient(port = 5555, eventLoopGroup = eventLoopGroup)
            client.scheduleAtFixedRate(1000 / 50) {
                    client.write(Command(1, 1), "ping-" + a.andIncrement)
            }
        }
//    10.seconds().sleep()
}