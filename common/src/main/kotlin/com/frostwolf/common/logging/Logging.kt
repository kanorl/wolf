package com.frostwolf.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

fun <T : Any> T.getLogger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass)) }
}

fun <T : Any> unwrapCompanionClass(javaClass: Class<T>): Class<*> {
    return if (javaClass.enclosingClass != null && javaClass.enclosingClass.kotlin.companionObject?.java == javaClass) {
        javaClass.enclosingClass
    } else {
        javaClass
    }
}

val defaultLogger = LoggerFactory.getLogger("default")!!