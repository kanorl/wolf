package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class RewardDeserializer : StdDeserializer<Reward>(Reward::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Reward? {
        val jsonNode = p.codec.readTree<JsonNode>(p)
        val type = jsonNode.get("type").asInt()
        val clazz = RewardType.valueOf(type)?.type
        return p.codec.treeToValue(jsonNode, clazz)
    }
}

class RawRewardDeserializer : StdDeserializer<RawReward<*>>(RawReward::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): RawReward<*>? {
        val jsonNode = p.codec.readTree<JsonNode>(p)
        val type = jsonNode.get("type").asInt()
        val clazz = RewardType.valueOf(type)?.rawType
        return p.codec.treeToValue(jsonNode, clazz)
    }
}