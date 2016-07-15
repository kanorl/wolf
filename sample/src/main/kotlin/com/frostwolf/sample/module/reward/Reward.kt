package com.frostwolf.sample.module.reward

import com.frostwolf.common.lang.isNegative
import com.frostwolf.common.toJson
import java.util.*

abstract class Reward(val type: RewardType, val num: Int) {
    init {
        check(!num.isNegative, { "num must NOT be negative" })
    }

    override fun toString(): String {
        return this.toJson()
    }

    abstract fun mergeable(other: Reward): Boolean
    abstract fun copy(num: Int = this.num): Reward

    operator fun plus(num: Int): Reward = copy(this.num + num)
    operator fun minus(num: Int): Reward = copy(this.num - num)
    operator fun times(multiple: Int): Reward = copy(this.num * multiple)
}

class SimpleReward(type: RewardType, num: Int) : Reward(type, num) {
    override fun mergeable(other: Reward): Boolean = type == other.type

    override fun copy(num: Int): Reward = SimpleReward(type, num)
}

class ItemReward(val itemId: Int, num: Int) : Reward(RewardType.Item, num) {
    override fun mergeable(other: Reward): Boolean = other is ItemReward && this.itemId == other.itemId

    override fun copy(num: Int): ItemReward = ItemReward(itemId, num)
}

@Suppress("UNCHECKED_CAST")
fun <E : Reward> Collection<E>.merged(): List<E> {
    val merged = ArrayList<E>(this.size)
    this.forEach { r ->
        val index = merged.indexOfFirst { it.mergeable(r) }
        if (index == -1) merged.add(r.copy() as E) else merged[index] = (merged[index] + r.num) as E
    }
    return merged
}