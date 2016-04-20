package com.frost.io.netty.handler

import com.frost.common.event.Event
import com.frost.common.event.EventBus
import com.frost.common.logging.getLogger
import com.frost.common.scheduling.Scheduler
import com.frost.common.time.millis
import com.frost.io.Identity
import com.frost.io.netty.ChannelIdentifyTimeoutException
import com.frost.io.netty.ChannelReplacedException
import com.frost.io.netty.config.SocketSetting
import io.netty.channel.*
import io.netty.util.AttributeKey
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
@ChannelHandler.Sharable
class ChannelManager : ChannelDuplexHandler() {
    val logger by getLogger() // TODO session log

    @Autowired
    private lateinit var eventBus: EventBus
    @Autowired
    private lateinit var setting: SocketSetting
    @Autowired
    private lateinit var scheduler: Scheduler

    private val channelGroup = ConcurrentHashMap<ChannelId, ChannelHandlerContext>()
    private val identifiedChannelGroups = ConcurrentHashMap<Class<out Identity>, ConcurrentHashMap<Identity, ChannelHandlerContext>>()
    private val remover = ChannelFutureListener { channelClosed(it.channel()) }
    private val createTimeKey = AttributeKey.valueOf<Long>("createTime")

    @PostConstruct
    private fun init() {
        val closeDelay = setting.identifyTimeout
        scheduler.scheduleWithFixedDelay(closeDelay, "AnonymousChannelCleanUp") {
            val now = millis()
            channelGroup.values.filter { !it.identified() && now - (it.attr(createTimeKey).get() ?: 0) > closeDelay.millis }.forEach {
                it.fireExceptionCaught(ChannelIdentifyTimeoutException)
                it.close()
                logger.info("Close channel due to identify timeout: {}", it)
            }
        }
    }

    private fun channelClosed(channel: Channel) {
        channelGroup.remove(channel.id())?.let { check(it.channel() == channel) } ?: logger.error("Failed to remove Channel: {}", channel)
        val identity = channel.attr(identityKey).get()
        identity?.let {
            identifiedChannelGroups.remove(it, channel)
            eventBus.post(ChannelClosedEvent(it))
        }
        channel.closeFuture().removeListener(remover)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        ctx.attr(createTimeKey).setIfAbsent(millis())
        val prev = channelGroup.putIfAbsent(ctx.channel().id(), ctx)
        check(prev == null, { "Duplicate channel id: ${channel.id()}.(should never happen)" })
        channel.closeFuture().addListener(remover)
        super.channelActive(ctx)
    }

    fun bind(channelId: ChannelId, identity: Identity) {
        val channel = checkNotNull(channelGroup[channelId], { "Bind failed: Channel not exists" })

        val group = identifiedChannelGroups.computeIfAbsent(identity.javaClass, { ConcurrentHashMap<Identity, ChannelHandlerContext>() })
        val removed = group.remove(identity)
        removed?.let {
            it.attr(identityKey).remove()
            it.fireExceptionCaught(ChannelReplacedException)
            it.close()
        }
        channel.identity(identity)
        val prev = group.putIfAbsent(identity, channel)
        check(prev == null, { "Replace channel failed" })
        eventBus.post(ChannelBindEvent(identity, prev != null))
    }

    internal fun channel(channelId: ChannelId): Channel? = channelGroup[channelId]?.channel()
    internal fun channels(): Collection<Channel> = channelGroup.values.map { it.channel() }
    internal fun channel(identity: Identity): Channel? = identifiedChannelGroups[identity.javaClass]?.get(identity)?.channel()
    internal fun channels(type: Class<out Identity>): Collection<Channel> = identifiedChannelGroups[type]?.values?.map { it.channel() } ?: emptyList()
}

private val identityKey = AttributeKey.valueOf<Identity>("identity")
fun ChannelHandlerContext.identity(): Identity? {
    return this.attr(identityKey).get()
}

fun Channel.identity(): Identity? {
    return this.attr(identityKey).get()
}

private fun ChannelHandlerContext.identity(identity: Identity) {
    this.attr(identityKey).setIfAbsent(identity)?.let { throw IllegalStateException("identity[$identity] is already set.") }
}

fun ChannelHandlerContext.identified(): Boolean = this.identity() != null

data class ChannelClosedEvent(val identity: Identity) : Event
data class ChannelBindEvent(val identity: Identity, val rebind: Boolean) : Event

