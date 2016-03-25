package com.frost.io.netty.config

import com.frost.common.lang.settingToString
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "io-netty")
class SocketSetting {
    val host = "127.0.0.1"
    var port = 0
    val poolSize = Runtime.getRuntime().availableProcessors() * 2
    val options: Map<String, Any> = hashMapOf()
    val childOptions: Map<String, Any> = hashMapOf()
    val frameLengthMax = 1024
    val connectionsMax = 5000
    val msgNumPerSecond: Long = 20
    val compressThreshold: Int = 0
    val whiteList = emptyArray<String>()
    val idleSeconds = 60

    override fun toString(): String {

        val builder = StringBuilder()
        builder.append("Socket Settings: \r\n")
        this.javaClass.declaredFields.forEach { f -> builder.append("\t").append(f.name).append(": ").append(f.get(this)).append("\n") }
        return settingToString(this, "Socket Setting")
    }
}