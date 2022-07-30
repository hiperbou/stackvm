package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.TokenType
import com.hiperbou.vm.compiler.parser.expressions.Expression

class GroupParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        val expression = parser.parseExpression()
        parser.consume(TokenType.RIGHT_PAREN)
        return expression
    }
}