package com.frost.io.netty.codec

import com.frost.io.Command
import com.frost.io.Compressor
import com.frost.io.Response
import com.frost.io.netty.toArray
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import java.nio.ByteOrder

class ResponseDecoder(
        byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN,
        maxFrameLength: Int = 32 * 1024,
        lengthFieldOffset: Int = 0,
        lengthFieldLength: Int = 4,
        lengthAdjustment: Int = 0,
        initialBytesToStrip: Int = 4,
        failFast: Boolean = true,
        val compressor: Compressor? = null
) : LengthFieldBasedFrameDecoder(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast) {

    override fun decode(ctx: ChannelHandlerContext?, `in`: ByteBuf?): Any? {
        val frame = super.decode(ctx, `in`) as? ByteBuf ?: return null
        val needDecompress = frame.readByte() == 1.toByte()
        val original = frame.toArray()
        val data = Unpooled.wrappedBuffer(if (needDecompress) compressor?.decompress(original) ?: original else original)

        val module = data.readShort()
        val cmd = data.readByte()
        val code = data.readInt()
        return Response(Command(module, cmd), data.toArray(), code)
    }
}