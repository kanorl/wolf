package com.frost.io.netty

import com.frost.common.lang.IgnoreStackTraceException

class ChannelInboundTrafficExcessException(msg: String) : IgnoreStackTraceException(msg)

object ChannelReplacedException : IgnoreStackTraceException()

object ChannelIdentifyTimeoutException : IgnoreStackTraceException()
