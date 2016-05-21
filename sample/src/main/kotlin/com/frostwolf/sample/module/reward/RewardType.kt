package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.module.SimpleModule
import com.frostwolf.common.jacksonObjectMapper
import com.frostwolf.common.toJson
import com.frostwolf.common.toObj


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

//@JsonDeserialize(using = RewardDeserializer::class, `as` = Reward::class)
open class Reward(
        val type: RewardType) {

}

class ItemReward(type: RewardType) : Reward(type) {

}

fun main(args: Array<String>) {
    jacksonObjectMapper.registerModule(SimpleModule().addDeserializer(Reward::class.java, RewardDeserializer()))
    val json = ItemReward(RewardType.Item).toJson()
    println(json)
    println(json.toObj<Reward>().type)
}