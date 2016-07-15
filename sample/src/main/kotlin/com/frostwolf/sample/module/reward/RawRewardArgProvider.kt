package com.frostwolf.sample.module.reward

import org.springframework.stereotype.Component

interface RawRewardArgProvider<out T: Number> {
    val key: String
    fun value(playerId: Long): T
}

@Component
open class PlayerLvProvider : RawRewardArgProvider<Int> {
    override val key: String = "playerLv"

    override fun value(playerId: Long): Int = 10
}