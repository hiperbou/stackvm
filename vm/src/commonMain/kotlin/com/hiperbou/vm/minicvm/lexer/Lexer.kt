package com.hiperbou.vm.minicvm.lexer

import com.hiperbou.vm.minicvm.lexer.TokenType.*

class Lexer(private val source: String) {
    private var currentPosition = 0
    private var currentLine = 1

    private val keywords = mapOf(
        "int" to INT,
        "void" to VOID,
        "if" to IF,
        "else" to ELSE,
        "while" to WHILE,
        "return" to RETURN,
        "true" to TRUE,
        "false" to FALSE
    )

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (!isAtEnd()) {
            val start = currentPosition
            when (val char = advance()) {
                ' ' -> {} // Ignore whitespace
                '\r' -> {} // Ignore whitespace
                '\t' -> {} // Ignore whitespace
                '\n' -> currentLine++ // Ignore whitespace
                '/' -> {
                    if (match('/')) {
                        // Single-line comment
                        while (peek() != '\n' && !isAtEnd()) advance()
                    } else if (match('*')) {
                        // Multi-line comment
                        while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                            if (peek() == '\n') currentLine++
                            advance()
                        }
                        if (!isAtEnd()) advance() // consume '*'
                        if (!isAtEnd()) advance() // consume '/'
                    } else {
                        tokens.add(Token(DIVIDE, "/", currentLine))
                    }
                }
                '+' -> tokens.add(Token(PLUS, "+", currentLine))
                '-' -> tokens.add(Token(MINUS, "-", currentLine))
                '*' -> tokens.add(Token(MULTIPLY, "*", currentLine))
                '%' -> tokens.add(Token(MODULO, "%", currentLine))
                '=' -> tokens.add(Token(if (match('=')) EQ else ASSIGN, if (tokens.lastOrNull()?.type == ASSIGN) "==" else "=", currentLine))
                '!' -> tokens.add(Token(if (match('=')) NEQ else LOGICAL_NOT, if (tokens.lastOrNull()?.type == LOGICAL_NOT) "!=" else "!", currentLine))
                '<' -> tokens.add(Token(if (match('=')) LTE else LT, if (tokens.lastOrNull()?.type == LT) "<=" else "<", currentLine))
                '>' -> tokens.add(Token(if (match('=')) GTE else GT, if (tokens.lastOrNull()?.type == GT) ">=" else ">", currentLine))
                '&' -> if (match('&')) tokens.add(Token(LOGICAL_AND, "&&", currentLine)) else throw LexerException("Unexpected character: & at line $currentLine")
                '|' -> if (match('|')) tokens.add(Token(LOGICAL_OR, "||", currentLine)) else throw LexerException("Unexpected character: | at line $currentLine")
                ';' -> tokens.add(Token(SEMICOLON, ";", currentLine))
                '(' -> tokens.add(Token(LPAREN, "(", currentLine))
                ')' -> tokens.add(Token(RPAREN, ")", currentLine))
                '{' -> tokens.add(Token(LBRACE, "{", currentLine))
                '}' -> tokens.add(Token(RBRACE, "}", currentLine))
                '[' -> tokens.add(Token(LBRACKET, "[", currentLine))
                ']' -> tokens.add(Token(RBRACKET, "]", currentLine))
                ',' -> tokens.add(Token(COMMA, ",", currentLine))
                else -> {
                    if (char.isLetter() || char == '_') {
                        val identifier = extractIdentifier(char)
                        tokens.add(Token(keywords[identifier] ?: IDENTIFIER, identifier, currentLine))
                    } else if (char.isDigit()) {
                        val number = extractNumber(char)
                        tokens.add(Token(INTEGER_LITERAL, number, currentLine))
                    } else {
                        throw LexerException("Unexpected character: $char at line $currentLine")
                    }
                }
            }
        }
        tokens.add(Token(EOF, "", currentLine))
        return tokens
    }

    private fun extractIdentifier(firstChar: Char): String {
        val builder = StringBuilder().append(firstChar)
        while (!isAtEnd() && (peek().isLetterOrDigit() || peek() == '_')) {
            builder.append(advance())
        }
        return builder.toString()
    }

    private fun extractNumber(firstChar: Char): String {
        val builder = StringBuilder().append(firstChar)
        while (!isAtEnd() && peek().isDigit()) {
            builder.append(advance())
        }
        return builder.toString()
    }

    private fun isAtEnd(): Boolean = currentPosition >= source.length

    private fun advance(): Char = source[currentPosition++]

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[currentPosition] != expected) return false
        currentPosition++
        return true
    }

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[currentPosition]

    private fun peekNext(): Char = if (currentPosition + 1 >= source.length) '\u0000' else source[currentPosition + 1]
}

class LexerException(message: String) : RuntimeException(message)
