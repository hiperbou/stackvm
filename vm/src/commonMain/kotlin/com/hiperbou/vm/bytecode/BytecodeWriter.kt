package com.hiperbou.vm.bytecode

class BytecodeWriter {
    fun write(bytecode: IntArray): ByteArray {
        val headerSize = MAGIC.size + 1
        val result = ByteArray(headerSize + bytecode.size * 4)
        var offset = 0

        MAGIC.copyInto(result, offset)
        offset += MAGIC.size
        result[offset++] = VERSION

        for (instruction in bytecode) {
            result[offset++] = ((instruction shr 24) and 0xFF).toByte()
            result[offset++] = ((instruction shr 16) and 0xFF).toByte()
            result[offset++] = ((instruction shr 8) and 0xFF).toByte()
            result[offset++] = (instruction and 0xFF).toByte()
        }

        return result
    }
}
