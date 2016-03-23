package com.frost.io.netty.client

import com.frost.io.Command
import com.frost.io.Request
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import java.util.concurrent.TimeUnit

class NettyClient {
    private val bootstrap: Bootstrap = Bootstrap()
    private val eventLoopGroup: EventLoopGroup
    private val channelInitializer: ChannelInitializer<SocketChannel>
    private lateinit var channel: Channel

    constructor(eventLoopGroup: EventLoopGroup, channelInitializer: ChannelInitializer<SocketChannel> = ClientChannelInitializer(), options: Map<ChannelOption<Any>, Any> = mapOf()) {
        this.eventLoopGroup = eventLoopGroup
        this.channelInitializer = channelInitializer
        bootstrap.group(eventLoopGroup).channel(if (Epoll.isAvailable()) EpollSocketChannel::class.java else NioSocketChannel::class.java).handler(channelInitializer)
        for ((k, v) in options) {
            bootstrap.option(k, v)
        }
    }

    fun connect(port: Int, host: String = "127.0.0.1") {
        channel = bootstrap.connect(host, port).sync().channel()
    }

    fun close() {
        eventLoopGroup.shutdownGracefully().sync()
    }

    fun write(cmd: Command, msg: Any? = null) {
        channel.writeAndFlush(Request(cmd, msg, channel))
    }

    fun schedule(interval: Long, task: () -> Unit) {
        eventLoopGroup.scheduleAtFixedRate(task, 0, interval, TimeUnit.MILLISECONDS)
    }
}

fun main(args: Array<String>) {
    val client = NettyClient(NioEventLoopGroup())
    client.connect(8888)
    client.schedule(2000) {
        client.write(Command(1, 1), "ping")
    }
}