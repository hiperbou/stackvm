package com.hiperbou.vm.memory

class DeviceMapper(private val properties: DeviceProperties, private val block:(Memory)-> Memory) {
    fun mapTo(memoryMapper: MemoryMapper): Memory {
        val deviceMemory = memoryMapper.alloc(properties.size())
        val mappedDevice = block(deviceMemory)
        memoryMapper.push(mappedDevice)
        return mappedDevice
    }
}

fun interface DeviceProperties {
    fun size():Int
}

interface ReadMemoryRegister {
    fun onRead() {}
}

interface WriteMemoryRegister {
    fun onWrite(value: Int) {}
}

interface ReadWriteMemoryRegister: ReadMemoryRegister, WriteMemoryRegister

class KeyboardDevice(private val memory: Memory): Memory by memory {
    companion object {
        const val IsKeyPressed = 0
        const val Key = 1

        const val size = 2

        fun builder() = DeviceMapper({ size }) { KeyboardDevice(it) }
    }

    override fun size() = size

    private class KeyboardStatusMemoryRegister(private val memory: Memory): ReadMemoryRegister {
        override fun onRead() {
            val character = readCharacter()
            memory[IsKeyPressed] = character.toBool().toInt()
            memory[Key] = character
        }

        private fun Int.toBool() = this != 0
        private fun Boolean.toInt() = if (this) 1 else 0
        private fun readCharacter() = listOf(Char(0), 'a', 'b', 'c').random().code
    }

    private val keyboardStatusRegister = KeyboardStatusMemoryRegister(memory)

    override operator fun get(index: Int):Int {
        when(index) {
            IsKeyPressed -> keyboardStatusRegister.onRead()
            else -> {}
        }
        return memory[index]
    }
}