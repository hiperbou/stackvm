package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter

class NumberExpression(val number: Int) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append(number)
    }

    override fun solveExpression(): Int {
        return number
    }

    override fun compileExpression(programWriter: ProgramWriter) {
        programWriter.addLiteral(solveExpression())
    }
}