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
 *
 * Consumes tokens from the shared [TokenStream] and produces [AstNode.Expression] nodes.
 * Statement-level parsing is handled by [CParser], which delegates here for expressions.
 *
 * Registered parselets:
 *   Prefix: NUMBER, IDENTIFIER, MINUS (unary), BANG, LPAREN (group), PLUS_PLUS, MINUS_MINUS
 *   Infix:  PLUS, MINUS, STAR, SLASH, PERCENT,
 *           EQ_EQ, BANG_EQ, LT, GT, LT_EQ, GT_EQ,
 *           AMP_AMP, PIPE_PIPE,
 *           PLUS_PLUS, MINUS_MINUS (postfix),
 *           LPAREN (function call)
 */
class CExpressionParser(private val tokens: TokenStream) {

    private val prefixParselets = mutableMapOf<CTokenType, CPrefixParselet>()
    private val infixParselets  = mutableMapOf<CTokenType, CInfixParselet>()

    init {
        // --- Prefix parselets ---
        registerPrefix(CTokenType.NUMBER,      NumberParselet())
        registerPrefix(CTokenType.IDENTIFIER,  IdentifierParselet())
        registerPrefix(CTokenType.LPAREN,      GroupParselet())
        registerPrefix(CTokenType.MINUS,       UnaryMinusParselet())
        registerPrefix(CTokenType.BANG,        UnaryNotParselet())
        registerPrefix(CTokenType.PLUS_PLUS,   PreIncDecParselet(IncDecOperator.INC))
        registerPrefix(CTokenType.MINUS_MINUS, PreIncDecParselet(IncDecOperator.DEC))

        // --- Infix parselets: arithmetic ---
        registerInfix(CTokenType.PLUS,    BinaryOpParselet(CPrecedence.SUM,     leftAssoc = true,  BinaryOperator.ADD))
        registerInfix(CTokenType.MINUS,   BinaryOpParselet(CPrecedence.SUM,     leftAssoc = true,  BinaryOperator.SUB))
        registerInfix(CTokenType.STAR,    BinaryOpParselet(CPrecedence.PRODUCT, leftAssoc = true,  BinaryOperator.MUL))
        registerInfix(CTokenType.SLASH,   BinaryOpParselet(CPrecedence.PRODUCT, leftAssoc = true,  BinaryOperator.DIV))
        registerInfix(CTokenType.PERCENT, BinaryOpParselet(CPrecedence.PRODUCT, leftAssoc = true,  BinaryOperator.MOD))

        // --- Infix parselets: comparison ---
        registerInfix(CTokenType.EQ_EQ,   BinaryOpParselet(CPrecedence.EQUALITY,   leftAssoc = true, BinaryOperator.EQ))
        registerInfix(CTokenType.BANG_EQ, BinaryOpParselet(CPrecedence.EQUALITY,   leftAssoc = true, BinaryOperator.NE))
        registerInfix(CTokenType.LT,      BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.LT))
        registerInfix(CTokenType.GT,      BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.GT))
        registerInfix(CTokenType.LT_EQ,   BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.LTE))
        registerInfix(CTokenType.GT_EQ,   BinaryOpParselet(CPrecedence.COMPARISON, leftAssoc = true, BinaryOperator.GTE))

        // --- Infix parselets: logical ---
        registerInfix(CTokenType.AMP_AMP,   BinaryOpParselet(CPrecedence.AND, leftAssoc = true, BinaryOperator.AND))
        registerInfix(CTokenType.PIPE_PIPE, BinaryOpParselet(CPrecedence.OR,  leftAssoc = true, BinaryOperator.OR))

        // --- Postfix: ++ / -- ---
        registerInfix(CTokenType.PLUS_PLUS,   PostIncDecParselet(IncDecOperator.INC))
        registerInfix(CTokenType.MINUS_MINUS, PostIncDecParselet(IncDecOperator.DEC))

        // --- Assignment (right-associative) ---
        registerInfix(CTokenType.EQ,       AssignParselet())
        registerInfix(CTokenType.PLUS_EQ,  CompoundAssignParselet(BinaryOperator.ADD))
        registerInfix(CTokenType.MINUS_EQ, CompoundAssignParselet(BinaryOperator.SUB))
        registerInfix(CTokenType.STAR_EQ,  CompoundAssignParselet(BinaryOperator.MUL))
        registerInfix(CTokenType.SLASH_EQ, CompoundAssignParselet(BinaryOperator.DIV))

        // --- Function call ---
        registerInfix(CTokenType.LPAREN, CallParselet())
    }

    fun registerPrefix(type: CTokenType, parselet: CPrefixParselet) {
        prefixParselets[type] = parselet
    }

    fun registerInfix(type: CTokenType, parselet: CInfixParselet) {
        infixParselets[type] = parselet
    }

    /**
     * Parse an expression with the given minimum precedence.
     * Call with [minPrecedence] = 0 to parse a full expression.
     */
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

    /** Peek at the precedence of the next token without consuming it. */
    private fun currentPrecedence(): Int {
        return infixParselets[tokens.peek().type]?.precedence ?: CPrecedence.NONE
    }

    /** Consume the next token, asserting it has the expected type. */
    fun consume(expected: CTokenType): CToken {
        val token = tokens.consume()
        if (token.type != expected) {
            throw ParseException("Expected $expected but got '${token.text}' (${token.type}) at line ${token.line}, col ${token.column}")
        }
        return token
    }

    /** Consume the next token without type checking. */
    fun consume(): CToken = tokens.consume()

    /** Peek at the next token without consuming it. */
    fun peek(): CToken = tokens.peek()

    /** Check if the next token has the given type (without consuming). */
    fun check(type: CTokenType): Boolean = tokens.peek().type == type

    /** Consume the next token only if it matches [type]. Returns true if consumed. */
    fun match(type: CTokenType): Boolean {
        if (!check(type)) return false
        tokens.consume()
        return true
    }
}

// =============================================================================
// Prefix parselets
// =============================================================================

/** Parses an integer literal: `42` */
private class NumberParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val value = token.text.toIntOrNull()
            ?: throw ParseException("Invalid integer literal '${token.text}' at line ${token.line}")
        return AstNode.NumberLiteral(value)
    }
}

/** Parses a variable reference or a function call: `x` or `foo(...)` */
private class IdentifierParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        // Function call is handled as an infix parselet on LPAREN,
        // so here we just return an Identifier node.
        return AstNode.Identifier(token.text)
    }
}

/** Parses a grouped expression: `(expr)` */
private class GroupParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression()
        parser.consume(CTokenType.RPAREN)
        return expr
    }
}

/** Parses unary minus: `-expr` */
private class UnaryMinusParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression(CPrecedence.PREFIX)
        return AstNode.UnaryOp(UnaryOperator.NEG, expr)
    }
}

/** Parses logical not: `!expr` */
private class UnaryNotParselet : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val expr = parser.parseExpression(CPrecedence.PREFIX)
        return AstNode.UnaryOp(UnaryOperator.NOT, expr)
    }
}

/** Parses pre-increment/decrement: `++x` / `--x` */
private class PreIncDecParselet(private val op: IncDecOperator) : CPrefixParselet {
    override fun parse(parser: CExpressionParser, token: CToken): AstNode.Expression {
        val nameToken = parser.consume(CTokenType.IDENTIFIER)
        return AstNode.PreIncDec(op, nameToken.text)
    }
}

// =============================================================================
// Infix parselets
// =============================================================================

/** Parses a binary operator: `left op right` */
private class BinaryOpParselet(
    override val precedence: Int,
    private val leftAssoc: Boolean,
    private val op: BinaryOperator
) : CInfixParselet {
    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        // For left-associative operators, parse right side with same precedence (so equal-precedence binds left).
        // For right-associative (e.g. assignment), parse with precedence - 1.
        val right = parser.parseExpression(if (leftAssoc) precedence else precedence - 1)
        return AstNode.BinaryOp(left, op, right)
    }
}

/** Parses post-increment/decrement: `x++` / `x--` */
private class PostIncDecParselet(private val op: IncDecOperator) : CInfixParselet {
    override val precedence = CPrecedence.POSTFIX
    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        if (left !is AstNode.Identifier) {
            throw ParseException("Post-increment/decrement requires a variable, got $left")
        }
        return AstNode.PostIncDec(left.name, op)
    }
}

/**
 * Parses a function call: `name(arg1, arg2, ...)`.
 * The [left] expression must be an [AstNode.Identifier].
 * The opening `(` has already been consumed as the infix token.
 */
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

/**
 * Parses simple assignment: `name = expr` (right-associative).
 * The [left] expression must be an [AstNode.Identifier].
 */
private class AssignParselet : CInfixParselet {
    override val precedence = CPrecedence.ASSIGNMENT
    override fun parse(parser: CExpressionParser, left: AstNode.Expression, token: CToken): AstNode.Expression {
        if (left !is AstNode.Identifier) {
            throw ParseException("Assignment target must be a variable, got $left")
        }
        // Right-associative: parse right side with precedence - 1
        val value = parser.parseExpression(CPrecedence.ASSIGNMENT - 1)
        return AstNode.AssignExpr(left.name, value)
    }
}

/**
 * Parses compound assignment: `name op= expr` (right-associative).
 * The [left] expression must be an [AstNode.Identifier].
 */
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
