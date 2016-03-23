package com.frost.io.netty.client

import com.frost.common.lang.combine
import com.frost.common.logging.getLogger
import com.frost.io.Codec
import com.frost.io.Compressor
import com.frost.io.Request
import com.frost.io.Response
import com.frost.io.netty.toArray
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import java.util.*

@ChannelHandler.Sharable
class ClientHandler(val codec: Codec, val compressor: Compressor? = null, val compressThreshold: Int = 0) : ChannelDuplexHandler() {
    val logger by getLogger()

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        val response = msg as? Response<ByteArray> ?: return
        logger.info("receive message: {}: {}", response, codec.decode(response.msg!!, String.javaClass))
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
        val request = msg as? Request<*> ?: return
        val byteBuf = toByteBuf(request)
        super.write(ctx, byteBuf, promise)
    }

    private fun toByteBuf(request: Request<*>): Any {
        val bytes = combine(request.command.bytes, codec.encode(request.body))
        val (result, compressed) = if (compressThreshold > 0 && bytes.size > compressThreshold && compressor != null) {
            (compressor.compress(bytes) to true)
        } else {
            (bytes to false)
        }
        val buffer = PooledByteBufAllocator.DEFAULT.buffer(result.size + (if (compressed) 1 else 0))
        buffer.writeByte((if (compressed) 1 else 0))
        buffer.writeBytes(result)
        println(Arrays.toString(buffer.duplicate().toArray()))
        return buffer
    }
}