package com.frostwolf.sample.client

import com.frostwolf.common.time.seconds
import com.frostwolf.io.Command
import com.frostwolf.io.netty.client.NettyClient
import io.netty.channel.nio.NioEventLoopGroup
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    val a = AtomicInteger()
    val eventLoopGroup = NioEventLoopGroup(8)
    //    val client = NettyClient(port = 5555, eventLoopGroup = eventLoopGroup)
    //    client.connect()
    for (i in 1..1000) {
        val client = NettyClient(port = 5555, eventLoopGroup = eventLoopGroup)
        client.scheduleAtFixedRate(1000 / 50) {
            client.write(Command(1, 1), "ping-" + a.andIncrement)
        }
    }
    10.seconds.sleep()
}