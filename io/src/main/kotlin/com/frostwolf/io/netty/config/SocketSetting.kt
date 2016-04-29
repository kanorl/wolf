package com.frostwolf.io.netty.config

import com.frostwolf.common.Bytes
import com.frostwolf.common.lang.settingToString
import com.frostwolf.common.time.FiniteDuration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "io-netty")
class SocketSetting {
    var host = "127.0.0.1"
    var port = 0
    var options: Map<String, Any> = hashMapOf()
    var childOptions: Map<String, Any> = hashMapOf()
    lateinit var frameLengthMax: Bytes
    var connectionsMax = 5000
    var msgNumPerSecond = 20
    lateinit var compressThreshold: Bytes
    var whiteList = emptyArray<String>()
    lateinit var readTimeout: FiniteDuration
    lateinit var identifyTimeout: FiniteDuration
    var codec = ""

    override fun toString(): String {
        return settingToString(this, "Socket Setting")
    }
}