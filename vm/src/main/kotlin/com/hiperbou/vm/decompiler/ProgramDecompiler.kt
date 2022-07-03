package com.hiperbou.vm.decompiler

import com.hiperbou.vm.Instruction
import com.hiperbou.vm.InstructionsEnum

data class Literal(val value:Int):Instruction{
    override fun toString() = value.toString()
}

data class Label(val value:Int):Instruction{
    override fun toString() = value.toString()
}


class ProgramDecompiler {
    fun decompile(program:IntArray):List<Instruction> {
        val values = InstructionsEnum.values()
        var params = 0
        var label = false
        return program.map {
            if (params > 0) {
                params--
                if(label) Label(it) else Literal(it)
            } else {
                val instruction = values.first { v -> v.opcode == it }
                params = instruction.params
                label = instruction.label
                instruction
            }
        }
    }
}