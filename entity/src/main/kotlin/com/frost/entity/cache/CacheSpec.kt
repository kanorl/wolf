package com.frost.entity.cache

import java.lang.annotation.Inherited

@Inherited
annotation class CacheSpec(

        /**
         * 缓存容量
         */
        val size: Int = 0,

        /**
         * 容量系数
         */
        val sizeFactor: Double = 1.0,

        /**
         * 预加载条
         */
        val preLoad: String = "",

        /**
         * 持久化策略
         */
        val persistenceInterval: PersistencePolicy = PersistencePolicy.Immediately
)

enum class PersistencePolicy {

    /**
     * 立即
     */
    Immediately,

    /**
     * 定时
     */
    Scheduled
}