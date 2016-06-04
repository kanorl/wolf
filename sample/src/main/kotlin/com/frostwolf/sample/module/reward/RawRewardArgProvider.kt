package com.frostwolf.sample.module.reward

import org.springframework.stereotype.Component

interface RawRewardArgProvider {
    val key: String
    fun value(playerId: Long): Any
}

@Component
open class PlayerLvProvider : RawRewardArgProvider {
    override val key: String = "playerLv"

    override fun value(playerId: Long): Any = 10
}