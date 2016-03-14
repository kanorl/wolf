package com.frost.entity

import com.frost.common.concurrent.cpuNum
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@ConfigurationProperties("entity")
class EntitySetting {
    @NestedConfigurationProperty
    var cache: CacheSetting? = null
    @NestedConfigurationProperty
    var persistence: PersistSetting? = null

    @PostConstruct
    private fun init() {
        println()
    }

    val persistPoolSize by lazy { persistence!!.poolSize }
    val persistInterval by lazy { persistence!!.interval }
    val cacheSize by lazy { cache!!.size }
    val cacheStat by lazy { cache!!.stat }
}

class CacheSetting {
    var size = 0
    var stat = false
}

class PersistSetting {
    var interval = 60L
    var poolSize = cpuNum * 2
}