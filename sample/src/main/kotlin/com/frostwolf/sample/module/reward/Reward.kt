package com.frostwolf.sample.module.reward

import com.frostwolf.common.lang.isPositive
import com.frostwolf.common.toJson

open class Reward(private val _num: Int, val type: RewardType, val expression: String? = "") {
    init {
        check(_num.isPositive, { "num must be positive" })
        check(!(_num != 0 && !expression.isNullOrEmpty()))
        check(!(_num == 0 && expression.isNullOrEmpty()))
    }

    override fun toString(): String {
        return this.javaClass.simpleName + this.toJson()
    }

    val num: Int
        get() = expression?.let { throw UnsupportedOperationException() } ?: _num

    open fun mergable(other: Reward): Boolean = this.type == other.type
}

class ItemReward(val itemId: Int, num: Int) : Reward(num, RewardType.Item) {
    operator fun plus(num: Int): ItemReward = ItemReward(itemId, num = this.num + num)
    operator fun minus(num: Int): ItemReward = ItemReward(itemId, num = this.num - num)
    operator fun times(multiple: Int): ItemReward = ItemReward(itemId, num = this.num * multiple)
}

fun main(args: Array<String>) {
    val r = ItemReward(1, 1)
    println(r + 2)
    println(r * 2)

    val rewards = listOf(ItemReward(1, 1), ItemReward(1, 1))
    rewards.groupBy({ it.itemId }, {}).values.toList()
}