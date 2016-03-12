package com.frost.io.netty.handler

import com.frost.common.lang.bytes
import com.frost.common.lang.combine
import com.frost.common.logging.getLogger
import com.frost.io.netty.Response
import com.frost.io.netty.codec.Codec
import com.frost.io.netty.config.SocketSetting
import io.netty.buffer.ByteBuf
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.util.ReferenceCountUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ChannelWriter : ChannelOutboundHandlerAdapter() {
    val logger by getLogger()

    @Autowired
    private lateinit var channelManager: ChannelManager
    @Autowired
    private lateinit var socketSetting: SocketSetting
    @Autowired(required = false)
    private var compressor: Compressor? = null
    @Autowired
    private lateinit var codec: Codec<Any?>

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val message: Any = if (msg is Response) toByteBuf(msg) else msg
        super.write(ctx, message, promise)
    }

    fun write(channelId: ChannelId, msg: Any): ChannelFuture? = channelManager.channel(channelId)?.writeAndFlush(msg)

    fun write(identity: Long, msg: Any): ChannelFuture? = channelManager.channel(identity)?.writeAndFlush(msg)

    fun writeAll(response: Response) {
        writeAll(channelManager.onlinePlayerIds(), response)
    }

    fun writeAll(identities: Collection<Long>, response: Response, filter: (Long) -> Boolean = { true }) {
        val byteBuf = toByteBuf(response)
        identities.filter { filter(it) }
                .forEach { write(it, byteBuf.duplicate().retain()) }
        ReferenceCountUtil.release(byteBuf)
    }

    private fun toByteBuf(response: Response): ByteBuf {
        val bytes = combine(response.command.bytes, response.code.bytes(), codec.encode(response.msg))
        val data = if (bytes.size > socketSetting.compressThreshold) compressor?.compress(bytes) ?: bytes else bytes
        return PooledByteBufAllocator.DEFAULT.buffer(data.size).writeBytes(data)
    }
}

interface Compressor {
    fun compress(bytes: ByteArray): ByteArray
}