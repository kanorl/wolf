package com.frost.io

interface Param<T> {
    fun getValue(request: Request): T;
}