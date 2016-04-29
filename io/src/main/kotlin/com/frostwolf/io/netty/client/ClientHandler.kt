package com.frostwolf.io.netty.client

import com.frostwolf.common.lang.toInt
import com.frostwolf.common.logging.getLogger
import com.frostwolf.io.Codec
import com.frostwolf.io.Compressor
import com.frostwolf.io.Request
import com.frostwolf.io.Response
import com.google.common.primitives.Bytes
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise

@Suppress("UNCHECKED_CAST")
@ChannelHandler.Sharable
class ClientHandler(val codec: Codec, val compressor: Compressor? = null, val compressThreshold: Int = 0) : ChannelDuplexHandler() {
    val logger by getLogger()

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val response = msg as? Response<ByteArray> ?: return
        logger.info("receive message: {}: {}", response, codec.decode(response.msg!!, String::class.java))
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        val request = msg as? Request<*> ?: return
        val byteBuf = toByteBuf(request)
        super.write(ctx, byteBuf, promise)
    }

    private fun toByteBuf(request: Request<*>): Any {
        val bytes = Bytes.concat(request.command.bytes(), codec.encode(request.body))
        val (result, compressed) = if (compressThreshold > 0 && bytes.size > compressThreshold && compressor != null) {
            (compressor.compress(bytes) to true)
        } else {
            (bytes to false)
        }
        val buffer = PooledByteBufAllocator.DEFAULT.buffer(result.size + compressed.toInt())
        buffer.writeByte(compressed.toInt())
        buffer.writeBytes(result)
        return buffer
    }
}