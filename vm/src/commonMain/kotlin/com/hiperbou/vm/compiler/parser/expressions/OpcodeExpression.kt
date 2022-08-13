package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.Opcode
import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.Parser

class OpcodeExpression(val opcode: Opcode, private val currentLine:Int, private val parser: Parser) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append(opcode)
    }

    override fun solveExpression(): Int {
        return opcode.opcode
    }

    override fun compileExpression(programWriter:ProgramWriter) {
        programWriter.addInstruction(opcode, currentLine)
    }
}
