package com.hiperbou.vm.ccompiler.parser

import com.hiperbou.vm.ccompiler.lexer.CToken
import com.hiperbou.vm.ccompiler.lexer.CTokenType

/**
 * A buffered, peekable stream of [CToken]s shared between [CParser] and [CExpressionParser].
 *
 * Both the statement-level recursive descent parser and the Pratt expression parser
 * operate on the same token stream so they can hand off control seamlessly.
 */
class TokenStream(private val tokens: List<CToken>) {
    private var pos: Int = 0

    /** Return the current token without consuming it. */
    fun peek(): CToken {
        return if (pos < tokens.size) tokens[pos]
        else CToken(CTokenType.EOF, "", 0, 0)
    }

    /** Look ahead [distance] tokens (0 = current). */
    fun peekAt(distance: Int): CToken {
        val index = pos + distance
        return if (index < tokens.size) tokens[index]
        else CToken(CTokenType.EOF, "", 0, 0)
    }

    /** Consume and return the current token. */
    fun consume(): CToken {
        val token = peek()
        if (pos < tokens.size) pos++
        return token
    }

    /** Consume the current token only if it matches [type]. Returns true if consumed. */
    fun match(type: CTokenType): Boolean {
        if (peek().type != type) return false
        consume()
        return true
    }

    /** Check if the current token has the given type (without consuming). */
    fun check(type: CTokenType): Boolean = peek().type == type

    /** Consume the current token, asserting it has [type]. Throws [ParseException] otherwise. */
    fun expect(type: CTokenType): CToken {
        val token = consume()
        if (token.type != type) {
            throw ParseException("Expected $type but got '${token.text}' (${token.type}) at line ${token.line}, col ${token.column}")
        }
        return token
    }

    fun isAtEnd(): Boolean = peek().type == CTokenType.EOF
}
