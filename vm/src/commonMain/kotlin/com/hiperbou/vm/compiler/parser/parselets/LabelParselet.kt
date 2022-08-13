package com.hiperbou.vm.compiler.parser.parselets

import com.hiperbou.vm.compiler.parser.Parser
import com.hiperbou.vm.compiler.parser.Token
import com.hiperbou.vm.compiler.parser.expressions.Expression
import com.hiperbou.vm.compiler.parser.expressions.LabelExpression
import com.hiperbou.vm.compiler.parser.expressions.LabelWithValueExpression

class LabelParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        return LabelExpression(token.text, token.currentLine, parser)
    }
}

class LabelWithValueParselet : PrefixParselet {
    override fun parse(parser: Parser, token: Token): Expression {
        val right = parser.parseExpression()
        parser.labelResolver.addLabel(token.text, right.solveExpression()) { parser.debugLine(token.currentLine) }
        return LabelWithValueExpression(token.text, right, parser) //TODO: should return NOP and do things here as labels are not compiled
    }
}
