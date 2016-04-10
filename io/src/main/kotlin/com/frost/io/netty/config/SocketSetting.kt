package com.frost.io.netty.config

import com.frost.common.concurrent.cpuNum
import com.frost.common.lang.settingToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "io-netty")
class SocketSetting {
    var host = "127.0.0.1"
    var port = 0
    var poolSize = cpuNum * 2
    var options: Map<String, Any> = hashMapOf()
    var childOptions: Map<String, Any> = hashMapOf()
    var frameLengthMax = 1024
    var connectionsMax = 5000
    var msgNumPerSecond: Int = 0
    var compressThreshold: Int = 0
    var whiteList = emptyArray<String>()
    var readTimeoutSeconds = 60
    var heartbeatInterval = 60000L
    var anonymousChannelCloseDelay = 3000000
    var codec = ""

    override fun toString(): String {
        return settingToString(this, "Socket Setting")
    }
}