package com.frost.common.concurrent

abstract class NamedTask : Runnable {
    abstract val name: String

    override fun toString(): String = name
}