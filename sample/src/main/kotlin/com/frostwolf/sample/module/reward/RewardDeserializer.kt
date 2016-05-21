package com.frostwolf.sample.module.reward

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer


class RewardDeserializer : StdDeserializer<Reward>(Reward::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Reward? {
        val jsonNode = p.codec.readTree<JsonNode>(p)
        val type = jsonNode.get("type").asInt()
        val clazz = RewardType.valueOf(type)?.clazz
        return p.codec.treeToValue(jsonNode, clazz)
    }
}