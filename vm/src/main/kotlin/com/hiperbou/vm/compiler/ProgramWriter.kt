package com.hiperbou.vm.compiler

import com.hiperbou.vm.Opcode

interface ProgramWriter {
    fun addInstruction(opcode: Opcode, currentLine:Int = 0)
    fun addLiteral(value: Int)
    fun currentAddress():Int

    val program:MutableList<Int>
}

class DefaultProgramWriter(override val program:MutableList<Int> = mutableListOf()):ProgramWriter{
    override fun currentAddress() = program.size

    override fun addInstruction(opcode: Opcode, currentLine: Int) {
        program.add(opcode.opcode)
    }

    override fun addLiteral(value:Int) {
        program.add(value)
    }
}

class DebugProgramWriter(override val program:MutableList<Int> = mutableListOf(),
                         private val lineOpcodeMap:MutableMap<Int,Int> = mutableMapOf()
):ProgramWriter{
    override fun currentAddress() = program.size

    override fun addInstruction(opcode: Opcode, currentLine: Int) {
        println("instruction $opcode at $currentLine")
        lineOpcodeMap.put(program.size, currentLine)
        program.add(opcode.opcode)
    }

    override fun addLiteral(value:Int) {
        program.add(value)
    }
}