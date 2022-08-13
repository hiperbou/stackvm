package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.PostfixExpression

class PostfixOperatorParselet(override val precedence: Int) : InfixParselet {
    override fun parse(parser: Parser, left: Expression, token: Token): Expression {
        return PostfixExpression(left, token.type)
    }

}