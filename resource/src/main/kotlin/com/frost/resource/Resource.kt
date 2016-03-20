package com.frost.resource

import com.frost.common.toJson
import com.google.common.collect.ComparisonChain
import javax.validation.constraints.Min

abstract class Resource : Comparable<Resource> {

    @Min(1)
    val id: Int = 0

    fun weight(): Int = 0

    override fun compareTo(other: Resource): Int = ComparisonChain.start().compare(this.javaClass.name, other.javaClass.name).compare(this.id, other.id).result()

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Resource
        if (id != other.id) return false
        return true
    }

    override fun toString(): String {
        return this.javaClass.simpleName + this.toJson()
    }
}