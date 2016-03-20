package com.frost

import com.frost.resource.Resource
import com.frost.resource.ValidateFailedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.validation.DataBinder
import org.springframework.validation.Validator
import javax.annotation.PostConstruct
import javax.validation.Valid

@Component
class T {
    @Autowired
    private lateinit var validator: Validator

    @PostConstruct
    fun init() {
        val item = Item()
        val bindingResult = DataBinder(item, "item").bindingResult
        validator.validate(item, bindingResult)
        if (bindingResult.hasErrors()) {
            throw ValidateFailedException(bindingResult.allErrors)
        }
    }
}

internal class Item : Resource() {

    //    @Min(1)
    var num = 0

    //    @ReferTo(Reward::class)
    val rewardId: Int = 0
    //    @Valid

    //    @EachReferTo(Reward::class)
    var rewardIds: List<Int> = arrayListOf(1, 2)

    @Valid
    val reward = Reward()

    //    @Valid
    var r = mapOf(1 to Reward())
}

class Reward : Resource() {
    //    @Min(0)
    var num = 1
}