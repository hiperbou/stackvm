package com.hiperbou.vm.bytecode

class BytecodeReader {
    fun read(data: ByteArray): IntArray {
        val headerSize = MAGIC.size + 1

        if (data.size < headerSize) {
            throw InvalidBytecodeFileException("File too short to contain a valid header")
        }

        for (i in MAGIC.indices) {
            if (data[i] != MAGIC[i]) {
                throw InvalidBytecodeFileException("Invalid magic bytes: not a VMB file")
            }
        }

        val version = data[MAGIC.size]
        if (version != VERSION) {
            throw InvalidBytecodeFileException("Unsupported bytecode version: $version (expected $VERSION)")
        }

        val bodySize = data.size - headerSize
        if (bodySize % 4 != 0) {
            throw InvalidBytecodeFileException("Bytecode body size must be a multiple of 4")
        }

        val count = bodySize / 4
        val bytecode = IntArray(count)
        var offset = headerSize

        for (i in 0 until count) {
            bytecode[i] =
                ((data[offset].toInt() and 0xFF) shl 24) or
                ((data[offset + 1].toInt() and 0xFF) shl 16) or
                ((data[offset + 2].toInt() and 0xFF) shl 8) or
                (data[offset + 3].toInt() and 0xFF)
            offset += 4
        }

        return bytecode
    }
}
