package com.hiperbou.vm.bytecode

import java.io.File

object BytecodeFile {
    fun writeTo(file: File, bytecode: IntArray) {
        file.writeBytes(BytecodeWriter.write(bytecode))
    }

    fun readFrom(file: File): IntArray {
        return BytecodeReader.read(file.readBytes())
    }
}
