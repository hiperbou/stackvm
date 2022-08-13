package com.hiperbou.vm.memory

interface Memory {
    operator fun get(index: Int): Int
    operator fun set(index: Int, value: Int)
    fun size():Int

    fun getBackingArray():IntArray
}

class DefaultMemory(
    private val memory:IntArray = IntArray(UByte.MAX_VALUE.toInt()) //UByte 512 //UShort 65535
):Memory {
    override fun get(index: Int) = memory[index]
    override fun set(index: Int, value: Int)  {
        memory[index] = value
    }
    override fun size() = memory.size
    override fun getBackingArray() = memory
}
