package com.frost.io

import org.springframework.stereotype.Component
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FIELD

@Retention(RUNTIME)
@Target(CLASS)
@Component
annotation class Module(val value: Short)

@Retention(RUNTIME)
@Target(FIELD)
annotation class Cmd(val value: Byte)

@Retention(RUNTIME)
@Target(FIELD, CLASS)
annotation class IdentityRequired(val value: Boolean)