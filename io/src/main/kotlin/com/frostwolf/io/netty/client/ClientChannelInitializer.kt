package com.frostwolf.io.netty.client

import com.frostwolf.io.StringCodec
import com.frostwolf.io.netty.codec.ResponseDecoder
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LengthFieldPrepender

class ClientChannelInitializer : ChannelInitializer<SocketChannel>() {

    private val handler = ClientHandler(StringCodec)
    private val prepender = LengthFieldPrepender(4)

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().
                addLast(prepender).
                addLast(ResponseDecoder()).
                addLast(handler)
    }
}