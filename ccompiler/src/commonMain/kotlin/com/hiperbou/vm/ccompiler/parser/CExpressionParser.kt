package com.hiperbou.vm.ccompiler.parser

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.ast.BinaryOperator
import com.hiperbou.vm.ccompiler.ast.IncDecOperator
import com.hiperbou.vm.ccompiler.ast.UnaryOperator
import com.hiperbou.vm.ccompiler.lexer.CToken
import com.hiperbou.vm.ccompiler.lexer.CTokenType

class ParseException(message: String) : Exception(message)

/**
 * Pratt (top-down operator precedence) expression parser.
 */
class CExpressionParser(private val tokens: TokenStream) {

    private val prefixParselets = mutableMapOf<CTokenType, CPrefixParselet>()
    private val infixParselets = mutableMapOf<CTokenType, CInfixParselet>()

    init {
        registerPrefix(CTokenType.NUMBER, NumberParselet())
        registerPrefix(CTokenType.IDENTIFIER, IdentifierParselet())
        registerPrefix(CTokenType.LPAREN, GroupParselet())
        registerPrefix(CTokenType.MINUS, UnaryMinusParselet())
        registerPrefix(CTokenType.BANG, UnaryNotParselet())
        registerPrefix(CTokenType.BIT_NOT, UnaryBitNotParselet())
        registerPrefix(CTokenType.PLUS_PLUS, PreIncDecParselet(IncDecOperator.INC))
        registerPrefix(CTokenType.MINUS_MINUS, PreIncDecParselet(IncDecOperator.DEC))

        registerInfix(CTokenType.PLUS, BinaryOpParselet(CPrecedence.SUM, leftAssoc = true, BinaryOperator.ADD))
        registerInfix(CTokenType.MINUS, BinaryOpParselet(CPrecedence.SUM, leftAssoc = true, BinaryOperator.SUB))
        registerInfix(CTokenType.STAR, BinaryOpParselet(CPrecedence.PRODUCT, leftAssoc = true, BinaryOperator.MUL))
        registerInfix(CTokenType.SLASH, BinaryOpParselet(CPrecedence.PRODUCT, leftAssoc = true, BinaryOperator.DIV))
        registerInfix(CTokenType.PERCENT, BinaryOpParselet(CPrecedence.PRODUCT, leftAssoc = true, BinaryOperator.MOD))

        registerInfix(CTokenType.EQ_EQ, BinaryOpParselet(CPrecedence.EQUALITY, leftAssoc = true, BinaryOperator.EQ))
        registerInfix(CTokenType.BANG_EQ, BinaryOpParselet(CPrecedence.EQUALITY, leftAssoc = true, BinaryOperator.NE))
        registerInfix(CTokenType.LT, BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.LT))
        registerInfix(CTokenType.GT, BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.GT))
        registerInfix(CTokenType.LT_EQ, BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.LTE))
        registerInfix(CTokenType.GT_EQ, BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.GTE))

        registerInfix(CTokenType.BIT_AND, BinaryOpParselet(CPrecedence.BIT_AND, leftAssoc = true, BinaryOperator.BIT_AND))
        registerInfix(CTokenType.BIT_XOR, BinaryOpParselet(CPrecedence.BIT_XOR, leftAssoc = true, BinaryOperator.BIT_XOR))
        registerInfix(CTokenType.BIT_OR, BinaryOpParselet(CPrecedence.BIT_OR, leftAssoc = true, BinaryOperator.BIT_OR))

        registerInfix(CTokenType.AMP_AMP, BinaryOpParselet(CPrecedence.AND, leftAssoc = true, BinaryOperator.AND))
        registerInfix(CTokenType.PIPE_PIPE, BinaryOpParselet(CPrecedence.OR, leftAssoc = true, BinaryOperator.OR))
        registerInfix(CTokenType.QUESTION, TernaryParselet())

        registerInfix(CTokenType.PLUS_PLUS, PostIncDecParselet(IncDecOperator.INC))
        registerInfix(CTokenType.MINUS_MINUS, PostIncDecParselet(IncDecOperator.DEC))
        registerInfix(CTokenType.LPAREN, CallParselet())
        registerInfix(CTokenType.LBRACKET, ArrayIndexParselet())

        registerInfix(CTokenType.EQ, AssignParselet())
        registerInfix(CTokenType.PLUS_EQ, CompoundAssignParselet(BinaryOperator.ADD))
        registerInfix(CTokenType.MINUS_EQ, CompoundAssignParselet(BinaryOperator.SUB))
        registerInfix(CTokenType.STAR_EQ, CompoundAssignParselet(BinaryOperator.MUL))
        registerInfix(CTokenType.SLASH_EQ, CompoundAssignParselet(BinaryOperator.DIV))
    }

    fun registerPrefix(type: CTokenType, parselet: CPrefixParselet) {
        prefixParselets[type] = parselet
    }

    fun registerInfix(type: CTokenType, parselet: CInfixParselet) {
        infixParselets[type] = parselet
    }

    fun parseExpression(minPrecedence: Int = CPrecedence.NONE): AstNode.Expression {
        val token = tokens.consume()
        val prefix = prefixParselets[token.type]
            ?: throw ParseException("Unexpected token '${token.text}' at line ${token.line}, col ${token.column}")

        var left = prefix.parse(this, token)

        while (minPrecedence < currentPrecedence()) {
            val opToken = tokens.consume()
            val infix = infixParselets[opToken.type]
                ?: throw ParseException("Unexpected infix token '${opToken.text}' at line ${opToken.line}, col ${opToken.column}")
            left = infix.parse(this, left, opToken)
        }

        return left
    }

    private fun currentPrecedence(): Int {
        return infixParselets[tokens.peek().type]?.precedence ?: CPrecedence.NONE
    }

    fun consume(expected: CTokenType): CToken {
        val token = tokens.consume()
        if (token.type != expected) {
            throw ParseException("Expected $expected but got '${token.text}' (${token.type}) at line ${token.line}, col ${token.column}")
        }
        return token
    }

    fun consume(): CToken = tokens.consume()
    fun peek(): CToken = tokens.peek()
    fun check(type: CTokenType): Boolean = tokens.peek().type == type

    fun match(type: CTokenType): Boolean {
        if (!check(type)) return false
        tokens.consume()
        return true
    }
}

private class NumberParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val value = token.text.toIntOrNull()
            ?: throw ParseException("Invalid integer literal '${token.text}' at line ${token.line}")
        return AstNode.NumberLiteral(value)
    }
}

private class IdentifierParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        return AstNode.Identifier(token.text)
    }
}

private class GroupParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression()
        parser.consume(CTokenType.RPAREN)
        return expr
    }
}

private class UnaryMinusParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression(CPrecedence.PREFIX)
        return AstNode.UnaryOp(UnaryOperator.NEG, expr)
    }
}

private class UnaryNotParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression(CPrecedence.PREFIX)
        return AstNode.UnaryOp(UnaryOperator.NOT, expr)
    }
}

private class UnaryBitNotParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression(CPrecedence.PREFIX)
        return AstNode.UnaryOp(UnaryOperator.BIT_NOT, expr)
    }
}

private class PreIncDecParselet(private val op: IncDecOperator) : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val nameToken = parser.consume(CTokenType.IDENTIFIER)
        return AstNode.PreIncDec(op, nameToken.text)
    }
}

private class BinaryOpParselet(
    override val precedence: Int,
    private val leftAssoc: Boolean,
    private val op: BinaryOperator
) : CInfixParselet {
    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        val right = parser.parseExpression(if (leftAssoc) precedence else precedence - 1)
        return AstNode.BinaryOp(left, op, right)
    }
}

private class PostIncDecParselet(private val op: IncDecOperator) : CInfixParselet {
    override val precedence = CPrecedence.POSTFIX
    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        if (left !is AstNode.Identifier) {
            throw ParseException("Post-increment/decrement requires a variable, got $left")
        }
        return AstNode.PostIncDec(left.name, op)
    }
}

private class CallParselet : CInfixParselet {
    override val precedence = CPrecedence.CALL

    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        if (left !is AstNode.Identifier) {
            throw ParseException("Function call requires an identifier, got $left")
        }
        val args = mutableListOf<AstNode.Expression>()
        if (!parser.check(CTokenType.RPAREN)) {
            do {
                args.add(parser.parseExpression())
            } while (parser.match(CTokenType.COMMA))
        }
        parser.consume(CTokenType.RPAREN)
        return AstNode.FunctionCall(left.name, args)
    }
}

private class ArrayIndexParselet : CInfixParselet {
    override val precedence = CPrecedence.CALL

    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        if (left !is AstNode.Identifier) {
            throw ParseException("Array access requires an identifier, got $left")
        }
        val index = parser.parseExpression()
        parser.consume(CTokenType.RBRACKET)
        return AstNode.ArrayAccess(left.name, index)
    }
}

private class TernaryParselet : CInfixParselet {
    override val precedence = CPrecedence.TERNARY

    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        val thenExpr = parser.parseExpression()
        parser.consume(CTokenType.COLON)
        val elseExpr = parser.parseExpression(CPrecedence.TERNARY - 1)
        return AstNode.TernaryOp(left, thenExpr, elseExpr)
    }
}

private class AssignParselet : CInfixParselet {
    override val precedence = CPrecedence.ASSIGNMENT

    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        val value = parser.parseExpression(CPrecedence.ASSIGNMENT - 1)
        return when (left) {
            is AstNode.Identifier -> AstNode.AssignExpr(left.name, value)
            is AstNode.ArrayAccess -> AstNode.ArrayAssignExpr(left.name, left.index, value)
            else -> throw ParseException("Assignment target must be a variable or array element, got $left")
        }
    }
}

private class CompoundAssignParselet(private val op: BinaryOperator) : CInfixParselet {
    override val precedence = CPrecedence.ASSIGNMENT

    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        if (left !is AstNode.Identifier) {
            throw ParseException("Compound assignment target must be a variable, got $left")
        }
        val value = parser.parseExpression(CPrecedence.ASSIGNMENT - 1)
        return AstNode.CompoundAssignExpr(left.name, op, value)
    }
}
