package com.hiperbou.vm.collection

interface Stack<T>: Collection<T> {
    fun push(element:T)
    fun pop():T
    fun peek():T
    fun toArray():Array<Any?>
}

class StackImpl<T>(private val impl:ArrayDeque<T> = ArrayDeque()):Stack<T>, Collection<T> by impl {
    override fun push(element:T) = impl.addFirst(element)
    override fun pop():T = impl.removeFirst()
    override fun peek():T = impl.first()
    override fun toArray():Array<Any?> = impl.toArray()
    override fun toString() = impl.toString()
}