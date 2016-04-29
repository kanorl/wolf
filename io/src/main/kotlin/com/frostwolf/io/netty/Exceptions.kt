package com.frostwolf.io.netty

class ChannelInboundTrafficExcessException(msg: String) : com.frostwolf.common.lang.IgnoreStackTraceException(msg)

object ChannelReplacedException : com.frostwolf.common.lang.IgnoreStackTraceException()

object ChannelIdentifyTimeoutException : com.frostwolf.common.lang.IgnoreStackTraceException()
