package com.frost.common.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.helpers.MessageFormatter
import kotlin.reflect.companionObject

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

fun String.loggingFormat(vararg args: Any): String = if (args.isEmpty()) this else MessageFormatter.arrayFormat(this, args).message