package com.frostwolf.common.concurrent

import com.frostwolf.common.Identified
import com.frostwolf.common.Named

fun task(id: Any? = null, name: String, action: () -> Unit): Runnable =
        if (id == null) {
            object : Runnable, Named {
                override val name: String = name

                override fun run() = action()
            }
        } else {
            object : Runnable, Named, Identified<Any> {
                override val id: Any = id
                override val name: String = name

                override fun run() = action()
            }
        }