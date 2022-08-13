package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.OperatorExpression

class BinaryOperatorParselet(override val precedence: Int, private val isRight: Boolean) : InfixParselet {
    override fun parse(parser: Parser, left: Expression, token: Token): Expression {
        val right = parser.parseExpression(
            precedence - if (isRight) 1 else 0
        )
        return OperatorExpression(left, token.type, right)
    }
}