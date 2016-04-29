package com.frostwolf.common


interface Identified<out ID : Any> {
    val id: ID
}