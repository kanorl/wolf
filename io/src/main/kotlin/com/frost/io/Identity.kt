package com.frost.io

import java.util.concurrent.atomic.AtomicLong

interface Identity {
    val value: Long

    companion object {
        object Unknown : Identity {
            override val value: Long = 0
        }

        data class Player(override val value: Long) : Identity

        data class GM(override val value: Long = GM.generator.incrementAndGet()) : Identity {
            companion object {
                private val generator = AtomicLong()
            }
        }
    }
}