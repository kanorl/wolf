package com.frost.io.netty.handler

import io.netty.handler.traffic.ChannelTrafficShapingHandler

class ChannelTrafficController(readLimit: Long) : ChannelTrafficShapingHandler(0, readLimit, 1000, 10000) {

    override fun calculateSize(msg: Any?): Long = 1
}