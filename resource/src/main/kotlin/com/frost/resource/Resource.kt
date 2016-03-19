package com.frost.resource

interface Resource : Comparable<Resource> {

    val id: Int

    fun weight(): Int = 0

    override fun compareTo(other: Resource): Int = id.compareTo(other.id)
}