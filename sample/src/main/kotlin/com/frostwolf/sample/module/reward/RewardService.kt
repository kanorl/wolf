package com.frostwolf.sample.module.reward

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
open class RewardService {

    @Autowired
    private lateinit var ctx: ApplicationContext
    private lateinit var argProviders: Collection<RawRewardArgProvider<*>>

    @PostConstruct
    private fun init() {
        argProviders = ctx.getBeansOfType(RawRewardArgProvider::class.java).values
    }

    fun toRewards(playerId: Long, rawRewards: Collection<RawReward<*>>): List<Reward> {
        val args = argProviders.associateBy({ it.key }, { it.value(playerId) })
        return rawRewards.map { it.toReward(args) }
    }

    fun toCosts(playerId: Long, rawCosts: Collection<RawCost>): List<Cost> {
        val args = argProviders.associateBy({ it.key }, { it.value(playerId) })
        return rawCosts.map { it.toCost(args) }
    }
}