package com.hiperbou.vm.bytecode

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class BytecodeFormatTest {

    @Test
    fun testRoundTrip() {
        val original = intArrayOf(0x01, 0x02, 0x10, 0xFF, 0x0000FFFF, -1, Int.MAX_VALUE, Int.MIN_VALUE)
        val bytes = BytecodeWriter.write(original)
        val result = BytecodeReader.read(bytes)
        assertContentEquals(original, result)
    }

    @Test
    fun testRoundTripEmpty() {
        val original = intArrayOf()
        val bytes = BytecodeWriter.write(original)
        val result = BytecodeReader.read(bytes)
        assertContentEquals(original, result)
    }

    @Test
    fun testBadMagicBytes() {
        val valid = BytecodeWriter.write(intArrayOf(0x01))
        val bad = valid.copyOf()
        bad[0] = 0x00
        assertFailsWith<InvalidBytecodeFileException> {
            BytecodeReader.read(bad)
        }
    }

    @Test
    fun testBadVersion() {
        val valid = BytecodeWriter.write(intArrayOf(0x01))
        val bad = valid.copyOf()
        bad[MAGIC.size] = 0x99.toByte()
        assertFailsWith<InvalidBytecodeFileException> {
            BytecodeReader.read(bad)
        }
    }

    @Test
    fun testTooShort() {
        assertFailsWith<InvalidBytecodeFileException> {
            BytecodeReader.read(byteArrayOf(0x56, 0x4D))
        }
    }
}
