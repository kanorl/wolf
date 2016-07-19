package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.frostwolf.common.reflect.cast
import java.lang.reflect.ParameterizedType

enum class RewardType(
        val value: Int,
        val rawType: Class<out RawReward<*>>,
        val type: Class<out Reward> = rawType.genericSuperclass.cast<ParameterizedType>().actualTypeArguments[0].cast()
) {
    Gold(1, RawSimpleReward::class.java),
    Item(2, RawItemReward::class.java);

    companion object {
        fun valueOf(value: Int): RewardType? = RewardType.values().find { it.value == value }

        @JsonCreator
        @JvmStatic
        fun forValue(value: Int): RewardType? = valueOf(value)
    }

    @JsonValue
    fun jsonValue(): Int = value
}