package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.frostwolf.common.lang.ceil
import com.frostwolf.common.toJson
import com.frostwolf.common.toObj
import java.util.*

abstract class Reward(val type: RewardType, val num: Int) {
    init {
        check(num >= 0, { "num must NOT less than 0" })
    }

    open fun mergeable(other: Reward): Boolean = this.type == other.type
    abstract fun copy(num: Int = this.num): Reward

    operator fun plus(num: Int): Reward = copy(this.num + num)
    operator fun minus(num: Int): Reward = copy(this.num - num)
    operator fun times(multiple: Double): Reward = multiply(multiple, false)
    operator fun div(multiple: Double): Reward = divide(multiple, false)
    fun multiply(multiple: Double, ceil: Boolean): Reward = copy(if (ceil) (this.num * multiple).ceil.toInt() else (this.num * multiple).toInt())
    fun divide(divided: Double, ceil: Boolean): Reward = copy(if (ceil) (this.num / divided).ceil.toInt() else (this.num / divided).toInt())

    override fun toString(): String {
        return this.toJson()
    }
}

class Cost(val reward: Reward) {
    @JsonValue
    fun jsonValue(): String = reward.toJson()

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(json: String): Cost = Cost(json.toObj<Reward>())
    }
}

class SimpleReward(type: RewardType, num: Int) : Reward(type, num) {
    override fun copy(num: Int): SimpleReward = SimpleReward(type, num)
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