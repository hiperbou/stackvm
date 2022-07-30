package com.hiperbou.vm.compiler.parser.expressions

import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.compiler.parser.TokenType

class OperatorExpression(
    private val left: Expression,
    private val operator: TokenType,
    private val right: Expression
) : Expression {
    override fun print(builder: StringBuilder) {
        builder.append("(")
        left.print(builder)
        builder.append(" ").append(operator.punctuator).append(" ")
        right.print(builder)
        builder.append(")")
    }

    override fun solveExpression(): Int {
        return when(operator) {
            TokenType.PLUS -> left.solveExpression() + right.solveExpression()
            TokenType.MINUS -> left.solveExpression() - right.solveExpression()
            TokenType.ASTERISK -> left.solveExpression() * right.solveExpression()
            TokenType.SLASH -> left.solveExpression() / right.solveExpression()
            else -> throw Exception("unknown operator $operator")
        }
    }

    override fun compileExpression(programWriter: ProgramWriter) {
        programWriter.addLiteral(solveExpression())
    }
}