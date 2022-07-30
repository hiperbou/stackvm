package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.TokenType

class PrefixExpression(private val operator: TokenType, private val right: Expression) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append("(").append(operator.punctuator)
        right.print(builder)
        builder.append(")")
    }

    override fun solveExpression(): Int {
        return when(operator){
            TokenType.MINUS -> - right.solveExpression()
            else -> throw Exception("unknown operator $operator")
        }
    }

    override fun compileExpression(programWriter: ProgramWriter) {
        programWriter.addLiteral(solveExpression())
    }
}