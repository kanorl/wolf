package com.frost.io.netty.codec

import com.frost.io.Command
import com.frost.io.Compressor
import com.frost.io.Request
import com.frost.io.netty.toArray
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import java.nio.ByteOrder

class RequestDecoder(
        byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
        maxFrameLength: Int,
        lengthFieldOffset: Int = 0,
        lengthFieldLength: Int = 4,
        lengthAdjustment: Int = 0,
        initialBytesToStrip: Int = 4,
        failFast: Boolean = true,
        val compressor: Compressor? = null
) : LengthFieldBasedFrameDecoder(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast) {

    constructor(maxFrameLength: Int, lengthFieldOffset: Int, lengthFieldLength: Int, failFast: Boolean, compressor: Compressor? = null) : this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 4, failFast, compressor)

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf?): Any? {
        val frame = super.decode(ctx, `in`) as? ByteBuf ?: return null
        val needDecompress = frame.readByte() == 1.toByte()
        val original = frame.toArray()
        val data = Unpooled.wrappedBuffer(if (needDecompress) compressor?.decompress(original) ?: original else original)

        val module = data.readShort()
        val cmd = data.readByte()
        return Request(Command(module, cmd), data.toArray())
    }

    override fun extractFrame(ctx: ChannelHandlerContext?, buffer: ByteBuf?, index: Int, length: Int): ByteBuf? {
        return buffer?.slice(index, length)
    }
}