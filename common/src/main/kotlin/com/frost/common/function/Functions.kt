package com.frost.common.function

@Suppress("UNCHECKED_CAST")
operator fun <R> Function<R>.invoke(args: List<Any>): R? {
    return when (this) {
        is Function0<R> -> this()
        is Function1<*, R> -> (this as (Any) -> R)(args[0])
        is Function2<*, *, R> -> (this as Function2<Any, Any, R>)(args[0], args[1])
        is Function3<*, *, *, R> -> (this as Function3<Any, Any, Any, R>)(args[0], args[1], args[2])
        is Function4<*, *, *, *, R> -> (this as Function4<Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3])
        is Function5<*, *, *, *, *, R> -> (this as Function5<Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4])
        is Function6<*, *, *, *, *, *, R> -> (this as Function6<Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5])
        is Function7<*, *, *, *, *, *, *, R> -> (this as Function7<Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6])
        is Function8<*, *, *, *, *, *, *, *, R> -> (this as Function8<Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
        is Function9<*, *, *, *, *, *, *, *, *, R> -> (this as Function9<Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8])
        is Function10<*, *, *, *, *, *, *, *, *, *, R> -> (this as Function10<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9])
        is Function11<*, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function11<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10])
        is Function12<*, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function12<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11])
        is Function13<*, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function13<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12])
        is Function14<*, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function14<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13])
        is Function15<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function15<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14])
        is Function16<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function16<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15])
        is Function17<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function17<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16])
        is Function18<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function18<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17])
        is Function19<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function19<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18])
        is Function20<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function20<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19])
        is Function21<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function21<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20])
        is Function22<*, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, *, R> -> (this as Function22<Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, Any, R>)(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19], args[20], args[21])
        else -> throw UnsupportedOperationException()
    }
}

fun main(args: Array<String>) {
    val builder = StringBuilder()
    for (i in 0..22) {
        builder.append("is ").append("Function").append(i)
        builder.append("<")
        for (j in 0..(i - 1)) {
            builder.append("*, ")
        }
        builder.append("R> -> (this as Function").append(i).append("<")
        for (j in 0..(i - 1)) {
            builder.append("Any, ")
        }
        builder.append("R>)(")
        for (j in 0..(i - 1)) {
            builder.append("args[").append(j).append("]")
            if (j != (i - 1))
                builder.append(", ")
        }
        builder.append(")\n")
    }
    print(builder)
}