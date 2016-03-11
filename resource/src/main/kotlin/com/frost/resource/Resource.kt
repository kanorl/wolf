package com.frost.resource

abstract class Resource : Comparable<Resource> {

    abstract fun getId(): Int

    fun weight(): Int = 0

    internal fun afterPropertiesSet() {
    }

    override fun compareTo(other: Resource): Int = this.getId().compareTo(other.getId())

    override fun equals(other: Any?): Boolean = when {
        other == null -> false
        this === other -> true
        this.javaClass != other.javaClass -> false
        else -> (other as Resource).getId() == this.getId()
    }

    override fun hashCode(): Int = getId().hashCode()
}