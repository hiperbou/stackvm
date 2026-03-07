package com.hiperbou.vm.bytecode

import java.io.File

class BytecodeFile {
    private val writer = BytecodeWriter()
    private val reader = BytecodeReader()

    fun writeTo(file: File, bytecode: IntArray) {
        file.writeBytes(writer.write(bytecode))
    }

    fun readFrom(file: File): IntArray {
        return reader.read(file.readBytes())
    }
}
