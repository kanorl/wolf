package com.frost.io.netty.handler

import com.frost.common.compareTo
import com.frost.common.lang.toByteArray
import com.frost.common.lang.toInt
import com.frost.common.logging.getLogger
import com.frost.io.Codec
import com.frost.io.Compressor
import com.frost.io.Identity
import com.frost.io.Response
import com.frost.io.netty.config.SocketSetting
import com.google.common.primitives.Bytes
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
    private lateinit var codec: Codec

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        val message: Any = if (msg is Response<*>) toByteBuf(msg) else msg
        super.write(ctx, message, promise)
    }

    fun write(channelId: ChannelId, msg: Any): ChannelFuture? = channelManager.channel(channelId)?.writeAndFlush(msg)

    fun write(identity: Identity, msg: Any): ChannelFuture? = channelManager.channel(identity)?.writeAndFlush(msg)

    fun writeAllPlayers(response: Response<*>) = writeAll(channelManager.channels(Identity.Companion.Player::class.java), response)

    fun writePlayer(identity: Long, msg: Any): ChannelFuture? = channelManager.channel(Identity.Companion.Player(identity))?.writeAndFlush(msg)

    fun writePlayers(identities: Collection<Long>, response: Response<*>, filter: (Long) -> Boolean = { true }) {
        writeAll(identities.filter { filter(it) }.map { channelManager.channel(Identity.Companion.Player(it)) }, response)
    }

    fun writeAll(response: Response<*>) = writeAll(channelManager.channels(), response)

    fun writeAll(identities: Collection<Channel?>, response: Response<*>) {
        val byteBuf = toByteBuf(response)
        identities.forEach { it?.writeAndFlush(byteBuf.duplicate().retain()) }
        ReferenceCountUtil.release(byteBuf)
    }

    private fun toByteBuf(response: Response<*>): ByteBuf {
        val bytes = Bytes.concat(response.command.bytes(), response.code.toByteArray(), codec.encode(response.msg))
        val compressor = this.compressor
        val (result, compressed) = if (socketSetting.compressThreshold > 0 && bytes.size > socketSetting.compressThreshold && compressor != null) {
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