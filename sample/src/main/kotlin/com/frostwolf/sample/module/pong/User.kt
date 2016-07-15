package com.frostwolf.sample.module.pong

import com.frostwolf.entity.Entity

class User private constructor() : Entity<Long>() {
    override var id: Long = 0
    var name: String = ""
    var age: Int = 0

    constructor(id: Long, name: String, age: Int) : this() {
        this.id = id
        this.name = name
        this.age = age
    }
}