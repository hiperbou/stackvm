package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.PrefixExpression

class PrefixOperatorParselet(val precedence: Int) : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        val right = parser.parseExpression(precedence)
        return PrefixExpression(token.type, right)
    }
}