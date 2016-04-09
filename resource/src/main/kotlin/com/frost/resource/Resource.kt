package com.frost.resource

import com.frost.common.Ordered
import com.frost.common.toJson
import com.google.common.collect.ComparisonChain

abstract class Resource : Ordered {

    abstract val id: Int

    fun weight(): Int = 0

    override fun order(): Int = id

    fun afterPropertiesSet() {

    }

    override final fun compareTo(other: Ordered): Int {
        if (other !is Resource) {
            return 1
        }
        return ComparisonChain.start().compare(this.javaClass.name, other.javaClass.name).compare(this.order(), other.order()).compare(this.id, other.id).result()
    }

    override final fun hashCode(): Int = id.hashCode()

    override final fun equals(other: Any?): Boolean {
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