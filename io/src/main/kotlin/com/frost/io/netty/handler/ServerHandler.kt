package com.frost.io.netty.handler

import com.frost.common.logging.getLogger
import com.frost.io.Request
import com.frost.io.Response
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleStateEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ServerHandler : SimpleChannelInboundHandler<Request>() {
    val logger by getLogger()

    @Autowired
    private lateinit var manager: InvokerManager

    override fun channelRead0(ctx: ChannelHandlerContext, request: Request) {
        if (manager.identityRequired(request.command) && !ctx.channel().identified()) {
            return;
        }
        val invoker = manager.invoker(request.command)
        if (invoker == null) {
            logger.error("No processor for command[{}]", request.command)
            return
        }

        var result: Result<*>? = null
        try {
            result = invoker.invoke(request)
        } catch(e: Exception) {
            logger.error("Request[cmd=${request.command}, identity=${ctx.channel().identity()}] process failed", e)
        }
        if (invoker.responseOmit) {
            return
        }

        ctx.writeAndFlush(Response(request.command, result?.value, result?.code ?: -1))
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) ctx.close()
    }
}