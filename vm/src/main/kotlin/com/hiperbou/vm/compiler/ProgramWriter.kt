package com.hiperbou.vm.compiler

import com.hiperbou.vm.Opcode

interface ProgramWriter {
    fun addInstruction(opcode: Opcode)
    fun addLiteral(value: Int)
    fun currentAddress():Int
    fun setCurrentLineNumberProvider(currentLineNumberProvider:()->Int)

    val program:MutableList<Int>
}

class DefaultProgramWriter(override val program:MutableList<Int> = mutableListOf()):ProgramWriter{
    override fun currentAddress() = program.size

    override fun addInstruction(opcode: Opcode) {
        program.add(opcode.opcode)
    }

    override fun addLiteral(value:Int) {
        program.add(value)
    }

    override fun setCurrentLineNumberProvider(currentLineNumberProvider:()->Int) {}
}

class DebugProgramWriter(override val program:MutableList<Int> = mutableListOf(),
                         private val lineOpcodeMap:MutableMap<Int,Int> = mutableMapOf()
):ProgramWriter{
    private var _currentLineProvider:()->Int = { 0 }
    override fun currentAddress() = program.size

    override fun addInstruction(opcode: Opcode) {
        lineOpcodeMap.put(program.size, _currentLineProvider())
        program.add(opcode.opcode)
    }

    override fun addLiteral(value:Int) {
        program.add(value)
    }

    override fun setCurrentLineNumberProvider(currentLineNumberProvider:()->Int) {
        _currentLineProvider = currentLineNumberProvider
    }
}