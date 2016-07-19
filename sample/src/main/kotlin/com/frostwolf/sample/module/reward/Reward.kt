package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.frostwolf.common.lang.ceil
import com.frostwolf.common.lang.isNegative
import com.frostwolf.common.toJson
import com.frostwolf.common.toObj
import java.util.*

interface RewardCost<out T> {
    val type: RewardType
    val num: Int

    fun mergeable(other: RewardCost<*>): Boolean
    fun copy(num: Int = this.num): T

    operator fun plus(num: Int): T = copy(this.num + num)
    operator fun minus(num: Int): T = copy(this.num - num)
    operator fun times(multiple: Double): T = multiply(multiple, false)
    operator fun div(multiple: Double): T = divide(multiple, false)
    fun multiply(multiple: Double, ceil: Boolean): T = copy(if (ceil) (this.num * multiple).ceil.toInt() else (this.num * multiple).toInt())
    fun divide(divided: Double, ceil: Boolean): T = copy(if (ceil) (this.num / divided).ceil.toInt() else (this.num / divided).toInt())
}

abstract class Reward(override val type: RewardType, final override val num: Int) : RewardCost<Reward> {
    init {
        check(!num.isNegative, { "num must NOT be negative" })
    }

    override fun toString(): String {
        return this.toJson()
    }

    override fun mergeable(other: RewardCost<*>): Boolean = other is Reward && this.type == other.type
}

class Cost(val reward: Reward) : RewardCost<Cost> {
    override val type: RewardType = reward.type
    override val num: Int = reward.num

    override fun mergeable(other: RewardCost<*>): Boolean = other is Cost && reward.mergeable(other.reward)

    override fun copy(num: Int): Cost = Cost(reward.copy(num))

    @JsonValue
    fun jsonValue(): String = reward.toJson()

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromJson(json: String): Cost = Cost(json.toObj<Reward>())
    }
}

class SimpleReward(type: RewardType, num: Int) : Reward(type, num) {
    override fun copy(num: Int): Reward = SimpleReward(type, num)
}

class ItemReward(val itemId: Int, num: Int) : Reward(RewardType.Item, num) {
    override fun mergeable(other: RewardCost<*>): Boolean = other is ItemReward && this.itemId == other.itemId

    override fun copy(num: Int): ItemReward = ItemReward(itemId, num)
}

@Suppress("UNCHECKED_CAST")
fun <E : RewardCost<*>> Collection<E>.merged(): List<E> {
    val merged = ArrayList<E>(this.size)
    this.forEach { r ->
        val index = merged.indexOfFirst { it.mergeable(r) }
        if (index == -1) merged.add(r.copy() as E) else merged[index] = (merged[index] + r.num) as E
    }
    return merged
}