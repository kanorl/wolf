package com.frostwolf.io.netty.filter

import com.frostwolf.io.netty.config.SocketSetting
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.ipfilter.IpFilterRule
import io.netty.handler.ipfilter.IpFilterRuleType
import io.netty.handler.ipfilter.IpSubnetFilterRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.net.InetSocketAddress
import java.net.SocketAddress
import javax.annotation.PostConstruct

@Component
@ChannelHandler.Sharable
open class ChannelFilterManager : ChannelInboundHandlerAdapter() {
    @Autowired
    private lateinit var socketSetting: SocketSetting
    @Autowired
    private lateinit var ctx: ApplicationContext

    private lateinit var whiteListIpRules: List<IpFilterRule>
    private lateinit var filters: List<ChannelFilter>

    @PostConstruct
    private fun init() {
        whiteListIpRules = socketSetting.whiteList.map {
            IpSubnetFilterRule(it.replace('*', '0'), 32 - it.toCharArray().filter { it -> it == '*' }.count() * 8, IpFilterRuleType.ACCEPT)
        }
        filters = ctx.getBeansOfType(ChannelFilter::class.java).values.sorted()
    }

    override final fun channelRegistered(ctx: ChannelHandlerContext) {
        handleNewChannel(ctx)
        ctx.fireChannelRegistered()
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        handleNewChannel(ctx)
        ctx.fireChannelActive()
    }

    private fun handleNewChannel(ctx: ChannelHandlerContext): Boolean {
        ctx.pipeline().remove(this)

        val remoteAddress: SocketAddress? = ctx.channel().remoteAddress()
        if (remoteAddress is InetSocketAddress && whiteListIpRules.any { rule -> rule.matches(remoteAddress) }) {
            return true
        }

        for (filter in filters) {
            if (!filter.accept(ctx)) {
                filter.channelRejected(ctx)?.let { it.addListener(ChannelFutureListener.CLOSE) } ?: ctx.close()
                return false
            }
        }

        return true
    }
}