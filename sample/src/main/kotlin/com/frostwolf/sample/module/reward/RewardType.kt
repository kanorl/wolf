package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RewardType(val value: Int, val clazz: Class<out Reward>) {
    Item(1, ItemReward::class.java);

    companion object {
        fun valueOf(value: Int): RewardType? = RewardType.values().find { it.value == value }

        @JsonCreator
        @JvmStatic
        fun forValue(value: String): RewardType? = valueOf(value.toInt())
    }

    @JsonValue
    fun toJson(): String = value.toString()
}