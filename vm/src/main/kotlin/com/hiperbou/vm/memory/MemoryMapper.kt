package com.hiperbou.vm.memory

class AssignedMemory(private val memory:IntArray, private val offset:Int, private val size:Int) : Memory {
    override operator fun get(index: Int) = memory[offset + index]

    override operator fun set(index: Int, value: Int) {
        memory[offset + index] = value
    }

    override fun size() = size
    override fun getBackingArray() = memory
}

class MemoryMapper(private val memory:IntArray): Memory {
    private var currentMemoryOffset = 0

    private val registerMappings = mutableMapOf<IntRange, Memory>()

    fun map(deviceMapper: DeviceMapper) {
        deviceMapper.mapTo(this)
    }

    fun alloc(size:Int): Memory {
        return AssignedMemory(memory, currentMemoryOffset, size)
    }

    fun push(device: Memory) {
        val memoryRange = (currentMemoryOffset until(currentMemoryOffset + device.size()))
        registerMappings.put(memoryRange, device)
        currentMemoryOffset += device.size()
    }

    override fun get(index: Int): Int {
        val mapper = registerMappings.keys.find { index in it } ?: return memory[index]
        return registerMappings.getValue(mapper).get(index - mapper.first)
    }

    override fun set(index: Int, value: Int) {
        registerMappings.keys.find { index in it }?.let { mapper ->
            registerMappings.getValue(mapper).set(index - mapper.first, value)
            return
        }
        memory[index] = value
    }

    override fun size() = memory.size
    override fun getBackingArray() = memory
}