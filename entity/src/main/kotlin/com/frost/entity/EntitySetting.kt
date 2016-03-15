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
    var cache: CacheSetting = CacheSetting()
    @NestedConfigurationProperty
    var persistence: PersistSetting = PersistSetting()
    @NestedConfigurationProperty
    var id: IdSetting = IdSetting()

    @PostConstruct
    private fun init() {
        println()
    }

    val persistPoolSize by lazy { persistence.poolSize }
    val persistInterval by lazy { persistence.interval }
    val cacheSize by lazy { cache.size }
    val cacheStat by lazy { cache.stat }
    val platformBits by lazy { id.platformBits }
    val serverBits by lazy { id.serverBits }
}

class CacheSetting {
    var size = 5000
    var stat = false
}

class PersistSetting {
    var interval = 60L
    var poolSize = cpuNum * 2
}

class IdSetting {
    var platformBits = 11
    var serverBits = 14
}