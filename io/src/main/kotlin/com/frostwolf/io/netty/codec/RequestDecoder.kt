package com.frostwolf.io.netty.codec

import com.frostwolf.io.Command
import com.frostwolf.io.Compressor
import com.frostwolf.io.Request
import com.frostwolf.io.netty.toArray
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
        initialBytesToStrip: Int = lengthFieldLength,
        failFast: Boolean = true,
        val compressor: Compressor? = null
) : LengthFieldBasedFrameDecoder(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast) {

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf?): Any? {
        val frame = super.decode(ctx, `in`) as? ByteBuf ?: return null
        val needDecompress = frame.readByte() == 1.toByte()
        val original = frame.toArray()
        val data = Unpooled.wrappedBuffer(if (needDecompress) compressor?.decompress(original) ?: original else original)

        val module = data.readShort()
        val cmd = data.readByte()
        return Request(Command(module, cmd), data.toArray())
    }

    override fun extractFrame(ctx: ChannelHandlerContext, buffer: ByteBuf, index: Int, length: Int): ByteBuf = buffer.slice(index, length)
}