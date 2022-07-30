package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.Parser

class IdentifierExpression(val identifier: String, private val parser: Parser) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append(identifier)
    }

    override fun solveExpression(): Int {
        val value = parser.labelResolver.getLabelAddress(identifier)
        if(value != null) return value

        parser.labelResolver.addUnresolvedLabel(identifier, parser.programWriter.currentAddress())
        return parser.labelResolver.UNRESOLVED_JUMP_ADDRESS
    }

    override fun compileExpression(programWriter: ProgramWriter) {
        programWriter.addLiteral(solveExpression())
    }
}