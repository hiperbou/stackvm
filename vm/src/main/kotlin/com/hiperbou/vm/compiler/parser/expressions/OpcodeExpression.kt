package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.InvalidProgramException
import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.Parser

class OpcodeExpression(val opcode: String, private val parser: Parser) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append(opcode)
    }

    override fun solveExpression(): Int {
        return parser.opcodeInformation.tryGetOpcode(opcode)?.opcode ?: throw InvalidProgramException("Unresolved opcode $opcode at line ${parser.currentLine}")
    }

    override fun compileExpression(programWriter:ProgramWriter) {
        val opcode = parser.opcodeInformation.tryGetOpcode(opcode) ?: throw InvalidProgramException("Unresolved opcode $opcode at line ${parser.currentLine}")
        programWriter.addInstruction(opcode)
    }
}
