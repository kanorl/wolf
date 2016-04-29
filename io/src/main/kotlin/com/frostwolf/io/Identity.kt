package com.frostwolf.io

import java.util.concurrent.atomic.AtomicLong

sealed class Identity {
    abstract val value: Long

    object Unknown : Identity() {
        override val value: Long = 0
    }

    class Player(override val value: Long) : Identity()

    class GM(override val value: Long = generator.incrementAndGet()) : Identity() {
        companion object {
            private val generator = AtomicLong()
        }
    }

    class RemoteServer(override val value: Long = generator.incrementAndGet()) : Identity() {
        companion object {
            private val generator = AtomicLong()
        }
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other?.javaClass == this.javaClass && this.value == (other as Identity).value
    }

    override fun toString(): String {
        return this.javaClass.simpleName + "(id=" + value + ")"
    }
}