package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule

object RewardDeserializer : StdDeserializer<Reward>(Reward::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Reward? {
        val jsonNode = p.codec.readTree<JsonNode>(p)
        val type = jsonNode.get("type").asInt()
        val clazz = RewardType.valueOf(type)?.type
        return p.codec.treeToValue(jsonNode, clazz)
    }
}

object RawRewardDeserializer : StdDeserializer<RawReward<*>>(RawReward::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): RawReward<*>? {
        val jsonNode = p.codec.readTree<JsonNode>(p)
        val type = jsonNode.get("type").asInt()
        val clazz = RewardType.valueOf(type)?.rawType
        return p.codec.treeToValue(jsonNode, clazz)
    }
}

class RewardJacksonModule : SimpleModule() {
    init {
        addDeserializer(Reward::class.java, RewardDeserializer)
        addDeserializer(RawReward::class.java, RawRewardDeserializer)
    }
}