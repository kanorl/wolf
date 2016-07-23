package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.frostwolf.common.reflect.`as`
import java.lang.reflect.ParameterizedType

enum class RewardType(
        val value: Int,
        val rawType: Class<out RawReward<*>> = RawSimpleReward::class.java,
        val type: Class<out Reward> = rawType.genericSuperclass.`as`<ParameterizedType>().actualTypeArguments[0].`as`()
) {
    Gold(1),
    Item(2, RawItemReward::class.java);

    companion object {
        @JsonCreator
        @JvmStatic
        fun valueOf(value: Int): RewardType? = RewardType.values().find { it.value == value }
    }

    @JsonValue
    fun value(): Int = value
}