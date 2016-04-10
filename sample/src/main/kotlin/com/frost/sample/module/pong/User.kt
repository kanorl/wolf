package com.frost.sample.module.pong

import com.frost.entity.IEntity

class User() : IEntity<Long>() {
    override var id: Long = 0
    var name: String = ""
    var age: Int = 0

    constructor(id: Long, name: String, age: Int) : this() {
        this.id = id
        this.name = name
        this.age = age
    }
}