package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.LabelExpression
import com.hiperbou.vm.compiler.parser.expressions.LabelWithValueExpression

class LabelParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        return LabelExpression(token.text, parser)
    }
}

class LabelWithValueParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        val right = parser.parseExpression()
        val currentLine = parser.currentLine
        val inputLine = "?" //TODO:
        parser.labelResolver.addLabel(token.text, right.solveExpression(), currentLine, inputLine)
        return LabelWithValueExpression(token.text, right, parser) //TODO: should return NOP and do things here as labels are not compiled
    }
}
