package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.TokenType

class PostfixExpression(private val left: Expression, private val operator: TokenType) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append("(")
        left.print(builder)
        builder.append(operator.punctuator).append(")")
    }

    override fun solveExpression(): Int {
        return when(operator) {
            else -> throw Exception("unknown operator $operator")
        }
    }

    override fun compileExpression(programWriter: ProgramWriter) {
        when(operator) {
            else -> throw Exception("unknown operator $operator")
        }
    }
}