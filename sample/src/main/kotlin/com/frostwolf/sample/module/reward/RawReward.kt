package com.frostwolf.sample.module.reward

import com.frostwolf.common.el.eval
import com.frostwolf.common.toJson

abstract class RawReward<T : Reward>(val type: RewardType, private val num: String) {
    @Transient
    private val count = try {
        num.toInt()
    } catch(e: NumberFormatException) {
        -1
    }

    final protected fun getNum(args: Map<String, Any>): Int = if (count == -1) eval(num, args) else count
    abstract fun toReward(args: Map<String, Any>): T

    override fun toString(): String = this.toJson()
}

class RawSimpleReward(type: RewardType, num: String) : RawReward<SimpleReward>(type, num) {
    override fun toReward(args: Map<String, Any>): SimpleReward = SimpleReward(type, getNum(args))
}

class RawItemReward(val itemId: Int, num: String) : RawReward<ItemReward>(RewardType.Item, num) {
    override fun toReward(args: Map<String, Any>): ItemReward = ItemReward(itemId, getNum(args))
}