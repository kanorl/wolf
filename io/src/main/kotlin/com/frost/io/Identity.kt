package com.frost.io

import java.util.concurrent.atomic.AtomicLong

interface Identity {
    val value: Long

    companion object {
        data class Player(override val value: Long) : Identity

        data class GM(override val value: Long) : Identity {
            companion object {
                private val generator = AtomicLong()
                fun create(): GM = GM(generator.incrementAndGet())
            }
        }
    }
}