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
        "false" to FALSE,
        "do" to DO,         // New keyword
        "for" to FOR,       // New keyword
        "break" to BREAK,   // New keyword
        "continue" to CONTINUE // New keyword
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
                        tokens.add(Token(DIVIDE, "/", currentLine)) // Original: DIVIDE
                    }
                }
                '+' -> {
                    if (match('+')) {
                        tokens.add(Token(INCREMENT, "++", currentLine)) // New: INCREMENT
                    } else {
                        tokens.add(Token(PLUS, "+", currentLine)) // Original: PLUS
                    }
                }
                '-' -> {
                    if (match('-')) {
                        tokens.add(Token(DECREMENT, "--", currentLine)) // New: DECREMENT
                    } else {
                        tokens.add(Token(MINUS, "-", currentLine)) // Original: MINUS
                    }
                }
                '*' -> tokens.add(Token(MULTIPLY, "*", currentLine)) // Original: MULTIPLY
                '%' -> tokens.add(Token(MODULO, "%", currentLine)) // Original: MODULO
                '=' -> tokens.add(Token(if (match('=')) EQ else ASSIGN, if (tokens.lastOrNull()?.type == ASSIGN) "==" else "=", currentLine)) // Original: EQ, ASSIGN
                '!' -> tokens.add(Token(if (match('=')) NEQ else LOGICAL_NOT, if (tokens.lastOrNull()?.type == LOGICAL_NOT) "!=" else "!", currentLine)) // Original: NEQ, LOGICAL_NOT
                '<' -> tokens.add(Token(if (match('=')) LTE else LT, if (tokens.lastOrNull()?.type == LT) "<=" else "<", currentLine)) // Original: LTE, LT
                '>' -> tokens.add(Token(if (match('=')) GTE else GT, if (tokens.lastOrNull()?.type == GT) ">=" else ">", currentLine)) // Original: GTE, GT
                '&' -> {
                    if (match('&')) {
                        tokens.add(Token(LOGICAL_AND, "&&", currentLine)) // Original: LOGICAL_AND
                    } else {
                        tokens.add(Token(BITWISE_AND, "&", currentLine)) // New: BITWISE_AND
                    }
                }
                '|' -> {
                    if (match('|')) {
                        tokens.add(Token(LOGICAL_OR, "||", currentLine)) // Original: LOGICAL_OR
                    } else {
                        tokens.add(Token(BITWISE_OR, "|", currentLine)) // New: BITWISE_OR
                    }
                }
                '^' -> tokens.add(Token(BITWISE_XOR, "^", currentLine)) // New: BITWISE_XOR
                '~' -> tokens.add(Token(BITWISE_NOT, "~", currentLine)) // New: BITWISE_NOT
                '?' -> tokens.add(Token(QMARK, "?", currentLine))       // New: QMARK
                ':' -> tokens.add(Token(COLON, ":", currentLine))       // New: COLON
                ';' -> tokens.add(Token(SEMICOLON, ";", currentLine)) // Original: SEMICOLON
                '(' -> tokens.add(Token(LPAREN, "(", currentLine))     // Original: LPAREN
                ')' -> tokens.add(Token(RPAREN, ")", currentLine))     // Original: RPAREN
                '{' -> tokens.add(Token(LBRACE, "{", currentLine))     // Original: LBRACE
                '}' -> tokens.add(Token(RBRACE, "}", currentLine))     // Original: RBRACE
                '[' -> tokens.add(Token(LBRACKET, "[", currentLine))   // Original: LBRACKET
                ']' -> tokens.add(Token(RBRACKET, "]", currentLine))   // Original: RBRACKET
                ',' -> tokens.add(Token(COMMA, ",", currentLine))       // Original: COMMA
                else -> {
                    if (char.isLetter() || char == '_') {
                        val identifier = extractIdentifier(char)
                        tokens.add(Token(keywords[identifier] ?: IDENTIFIER, identifier, currentLine))
                    } else if (char.isDigit()) {
                        val number = extractNumber(char)
                        tokens.add(Token(INTEGER_LITERAL, number, currentLine)) // Original: INTEGER_LITERAL
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
