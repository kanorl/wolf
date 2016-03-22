package com.frost.io.netty.codec

import com.frost.io.Command
import com.frost.io.Request
import com.frost.io.netty.toArray
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import java.nio.ByteOrder

class RequestDecoder(byteOrder: ByteOrder?, maxFrameLength: Int, lengthFieldOffset: Int, lengthFieldLength: Int, lengthAdjustment: Int, initialBytesToStrip: Int, failFast: Boolean) : LengthFieldBasedFrameDecoder(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast) {

    constructor(maxFrameLength: Int, lengthFieldOffset: Int, lengthFieldLength: Int, failFast: Boolean) : this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0, failFast)

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf?): Any? {
        val byteBuf = super.decode(ctx, `in`) ?: return null
        if (byteBuf !is ByteBuf) {
            return null;
        }

        val module = byteBuf.readShort()
        val cmd = byteBuf.readByte()

        return Request(Command(module, cmd), byteBuf.toArray(), ctx.channel())
    }

    override fun extractFrame(ctx: ChannelHandlerContext?, buffer: ByteBuf?, index: Int, length: Int): ByteBuf? {
        return buffer?.slice(index, length)
    }
}