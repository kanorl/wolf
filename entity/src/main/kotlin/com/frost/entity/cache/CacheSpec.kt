package com.frost.entity.cache

import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
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
         * 预加载
         */
        val preLoad: String = "",

        /**
         * 持久化间隔: 60000ms 60s 1m
         */
        val persistInterval: String = ""
)