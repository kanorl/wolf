package com.frost.io

import io.netty.channel.Channel

interface Param<T> {
    fun getValue(request: Request<*>, channel: Channel): T;
}