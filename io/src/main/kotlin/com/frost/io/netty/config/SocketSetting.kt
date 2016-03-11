package com.frost.io.netty.config

import org.apache.commons.lang3.SystemUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "io-netty")
class SocketSetting {
    var host = "127.0.0.1"
    var port = 8888
    var poolSize = Runtime.getRuntime().availableProcessors() * 2
    var options: Map<String, Any> = hashMapOf("ALLOCATOR" to "pooled")
    var childOptions: Map<String, Any> = hashMapOf("ALLOCATOR" to "pooled")
    var frameLengthMax = 1024
    var connectionsMax = 5000
    var msgNumPerSecond: Long = 20
    var compressThreshold: Int = 0
    var whiteList = emptyArray<String>()
    var idleSeconds = 60
    var codec = "un_set"

    override fun toString(): String {

        val builder = StringBuilder()
        builder.append("Socket Settings: \r\n")
        this.javaClass.declaredFields.forEach { f -> builder.append("\t").append(f.name).append(": ").append(f.get(this)).append("\n") }
        return ToStringBuilder.reflectionToString(this, object : ToStringStyle() {
            init {
                fieldNameValueSeparator = ": "
                isUseClassName = false
                isUseIdentityHashCode = false
                fieldSeparator = SystemUtils.LINE_SEPARATOR
                isFieldSeparatorAtStart = true
                contentStart = "_______________________________Socket Settings______________________________"
                contentEnd = "\n____________________________________________________________________________\n"
                arrayStart = "["
                arrayEnd = "]"
            }
        })
    }
}