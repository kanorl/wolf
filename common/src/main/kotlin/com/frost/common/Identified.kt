package com.frost.common


interface Identified<out ID: Any> {
    val id: ID
}