package com.frost.io.netty.client

import com.frost.io.StringCodec
import com.frost.io.netty.codec.ResponseDecoder
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