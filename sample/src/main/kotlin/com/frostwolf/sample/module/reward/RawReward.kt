package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.frostwolf.common.el.eval
import com.frostwolf.common.toJson
import com.frostwolf.common.toObj

abstract class RawReward<out T : Reward>(val type: RewardType, private val num: String) {
    @Transient
    private val count = try {
        num.toInt()
    } catch(e: NumberFormatException) {
        -1
    }

    protected fun getNum(args: Map<String, Any>): Int = if (count == -1) eval(num, args) else count
    abstract fun toReward(args: Map<String, Any>): T

    override fun toString(): String = this.toJson()
}


class RawCost(val rawReward: RawReward<*>) {

    fun toCost(args: Map<String, Any>): Cost = Cost(rawReward.toReward(args))

    @JsonValue
    fun jsonValue(): String = rawReward.toJson()

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(json: String): RawCost = RawCost(json.toObj<RawReward<*>>())
    }
}

class RawSimpleReward(type: RewardType, num: String) : RawReward<SimpleReward>(type, num) {
    override fun toReward(args: Map<String, Any>): SimpleReward = SimpleReward(type, getNum(args))
}

class RawItemReward(val itemId: Int, num: String) : RawReward<ItemReward>(RewardType.Item, num) {
    override fun toReward(args: Map<String, Any>): ItemReward = ItemReward(itemId, getNum(args))
}