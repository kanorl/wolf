package com.frostwolf.io.netty

import com.frostwolf.common.lang.NoStackTraceException

class ChannelInboundTrafficExcessException(msg: String) : NoStackTraceException(msg)

object ChannelReplacedException : NoStackTraceException()

object ChannelIdentifyTimeoutException : NoStackTraceException()
