package com.hiperbou.vm.memory

import kotlin.test.*

class MemoryMapperTest {

    private fun memory() = IntArray(UByte.MAX_VALUE.toInt())

    @Test
    fun memoryMapperReadAllZeroesTest() {
        val memory = memory()

        val memoryMapper = MemoryMapper(memory)
        memory.forEachIndexed { index, _ ->
            assertEquals(0, memoryMapper[index])
        }
    }

    @Test
    fun memoryMapperWriteAllTest() {
        val memory = memory()

        val memoryMapper = MemoryMapper(memory)
        memory.forEachIndexed { index, _ ->
            memoryMapper[index] = index
        }
        memory.forEachIndexed { index, _ ->
            assertEquals(index, memoryMapper[index])
        }
    }

    @Test
    fun memoryMapperAllocDummyMapperTest() {
        val memory = memory()

        val memoryMapper = MemoryMapper(memory)
        with(memoryMapper) {
            push(alloc(16)) // we save memory for 16 registers
        }
        memory.forEachIndexed { index, _ ->
            assertEquals(0, memoryMapper[index])
        }
        memory.forEachIndexed { index, _ ->
            memoryMapper[index] = index
        }
        memory.forEachIndexed { index, _ ->
            assertEquals(index, memoryMapper[index])
        }
    }

    @Test
    fun memoryMapperDummyWriteMapperTest() {
        val memory = memory()

        val memoryMapper = MemoryMapper(memory)
        with(memoryMapper) {
            push(alloc(16))
            push(alloc(16))
            push(alloc(16))
        }
        memory.forEachIndexed { index, _ ->
            assertEquals(0, memoryMapper[index])
        }
        memory.forEachIndexed { index, _ ->
            memoryMapper[index] = index
        }
        memory.forEachIndexed { index, _ ->
            assertEquals(index, memoryMapper[index])
        }
    }

    @Test
    fun memoryMapperDeviceMapperTest() {
        val memory = memory()

        memory[0] = -1
        memory[1] = -1

        val memoryMapper = MemoryMapper(memory)

        assertEquals(-1, memoryMapper[KeyboardDevice.IsKeyPressed])
        assertEquals(-1 , memoryMapper[KeyboardDevice.Key])

        with(memoryMapper) {
            map(KeyboardDevice.builder())
        }
        assertEquals(-1 , memoryMapper[KeyboardDevice.Key])

        val keyValues = listOf('a', 'b', 'c').map { it.code }

        repeat(10) {
            val isKeyPressed = memoryMapper[KeyboardDevice.IsKeyPressed]
            val key = memoryMapper[KeyboardDevice.Key]
            assertTrue( isKeyPressed == 0 || isKeyPressed == 1)
            if (isKeyPressed == 1) {
                assertTrue(keyValues.contains(key))
            } else {
                assertEquals(0, key)
            }
        }
    }

    @Test
    fun memoryMapperDummyAndDeviceMapperTest() {
        val memory = memory()

        val memoryMapper = MemoryMapper(memory)

        with(memoryMapper) {
            push(alloc(16)) // we save memory for 16 registers
            map(KeyboardDevice.builder())
        }

        (0 until 16).forEach { index ->
            memoryMapper[index] = index
        }
        (0 until 16).forEach { index ->
            assertEquals(index, memoryMapper[index])
        }

        val keyValues = listOf('a', 'b', 'c').map { it.code }

        val offset = 16 //TODO: find an elegant way to get the mapper offset

        repeat(10) {
            val isKeyPressed = memoryMapper[offset + KeyboardDevice.IsKeyPressed]
            val key = memoryMapper[offset + KeyboardDevice.Key]
            assertTrue( isKeyPressed == 0 || isKeyPressed == 1)
            if (isKeyPressed == 1) {
                assertTrue(keyValues.contains(key))
            } else {
                assertEquals(0, key)
            }
        }
    }

    @Test
    fun memoryMapperMultipleDummyAndDeviceMapperTest() {
        val memory = memory()

        val memoryMapper = MemoryMapper(memory)

        with(memoryMapper) {
            push(alloc(16))
            map(KeyboardDevice.builder())
            push(alloc(32))
            map(KeyboardDevice.builder())
        }

        (0 until 16).forEach { index ->
            memoryMapper[index] = index
        }
        (0 until 16).forEach { index ->
            assertEquals(index, memoryMapper[index])
        }

        (16 + 2 until 16 + 2 + 32).forEach { index ->
            memoryMapper[index] = index
        }
        (16 + 2 until 16 + 2 + 32).forEach { index ->
            assertEquals(index, memoryMapper[index])
        }

        val keyValues = listOf('a', 'b', 'c').map { it.code }

        var offset = 16 //TODO: find an elegant way to get the mapper offset

        repeat(10) {
            val isKeyPressed = memoryMapper[offset + KeyboardDevice.IsKeyPressed]
            val key = memoryMapper[offset + KeyboardDevice.Key]
            assertTrue( isKeyPressed == 0 || isKeyPressed == 1)
            if (isKeyPressed == 1) {
                assertTrue(keyValues.contains(key))
            } else {
                assertEquals(0, key)
            }
        }

        offset = 16 + 2 + 32 //TODO: find an elegant way to get the mapper offset

        repeat(10) {
            val isKeyPressed = memoryMapper[offset + KeyboardDevice.IsKeyPressed]
            val key = memoryMapper[offset + KeyboardDevice.Key]
            assertTrue( isKeyPressed == 0 || isKeyPressed == 1)
            if (isKeyPressed == 1) {
                assertTrue(keyValues.contains(key))
            } else {
                assertEquals(0, key)
            }
        }
    }
}