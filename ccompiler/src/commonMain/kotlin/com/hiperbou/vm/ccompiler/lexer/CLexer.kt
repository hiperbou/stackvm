package com.hiperbou.vm.ccompiler.lexer

class LexerException(message: String) : Exception(message)

class CLexer(private val source: String) {

    private var pos: Int = 0
    private var line: Int = 1
    private var column: Int = 1

    private val keywords: Map<String, CTokenType> = mapOf(
        "int" to CTokenType.INT,
        "return" to CTokenType.RETURN,
        "if" to CTokenType.IF,
        "else" to CTokenType.ELSE,
        "while" to CTokenType.WHILE,
        "for" to CTokenType.FOR,
        "do" to CTokenType.DO,
        "break" to CTokenType.BREAK,
        "continue" to CTokenType.CONTINUE,
        "print" to CTokenType.PRINT,
        "debugPrint" to CTokenType.DEBUG_PRINT
    )

    fun tokenize(): List<CToken> {
        val tokens = mutableListOf<CToken>()
        while (true) {
            val token = nextToken()
            tokens.add(token)
            if (token.type == CTokenType.EOF) break
        }
        return tokens
    }

    private fun peek(): Char = if (pos < source.length) source[pos] else '\u0000'
    private fun peekNext(): Char = if (pos + 1 < source.length) source[pos + 1] else '\u0000'

    private fun advance(): Char {
        val c = source[pos++]
        if (c == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return c
    }

    private fun match(expected: Char): Boolean {
        if (pos >= source.length || source[pos] != expected) return false
        advance()
        return true
    }

    private fun skipWhitespaceAndComments() {
        while (pos < source.length) {
            when {
                peek() == ' ' || peek() == '\t' || peek() == '\r' || peek() == '\n' -> advance()
                peek() == '/' && peekNext() == '/' -> {
                    while (pos < source.length && peek() != '\n') advance()
                }
                peek() == '/' && peekNext() == '*' -> {
                    advance(); advance()
                    while (pos < source.length) {
                        if (peek() == '*' && peekNext() == '/') {
                            advance(); advance()
                            break
                        }
                        advance()
                    }
                }
                else -> return
            }
        }
    }

    private fun nextToken(): CToken {
        skipWhitespaceAndComments()

        if (pos >= source.length) {
            return CToken(CTokenType.EOF, "", line, column)
        }

        val startLine = line
        val startCol = column
        val c = advance()

        return when {
            c.isDigit() -> readNumber(c, startLine, startCol)
            c.isLetter() || c == '_' -> readIdentifierOrKeyword(c, startLine, startCol)
            c == '+' -> when {
                match('+') -> CToken(CTokenType.PLUS_PLUS, "++", startLine, startCol)
                match('=') -> CToken(CTokenType.PLUS_EQ, "+=", startLine, startCol)
                else -> CToken(CTokenType.PLUS, "+", startLine, startCol)
            }
            c == '-' -> when {
                match('-') -> CToken(CTokenType.MINUS_MINUS, "--", startLine, startCol)
                match('=') -> CToken(CTokenType.MINUS_EQ, "-=", startLine, startCol)
                else -> CToken(CTokenType.MINUS, "-", startLine, startCol)
            }
            c == '*' -> if (match('=')) CToken(CTokenType.STAR_EQ, "*=", startLine, startCol) else CToken(CTokenType.STAR, "*", startLine, startCol)
            c == '/' -> if (match('=')) CToken(CTokenType.SLASH_EQ, "/=", startLine, startCol) else CToken(CTokenType.SLASH, "/", startLine, startCol)
            c == '%' -> CToken(CTokenType.PERCENT, "%", startLine, startCol)
            c == '=' -> if (match('=')) CToken(CTokenType.EQ_EQ, "==", startLine, startCol) else CToken(CTokenType.EQ, "=", startLine, startCol)
            c == '!' -> if (match('=')) CToken(CTokenType.BANG_EQ, "!=", startLine, startCol) else CToken(CTokenType.BANG, "!", startLine, startCol)
            c == '<' -> if (match('=')) CToken(CTokenType.LT_EQ, "<=", startLine, startCol) else CToken(CTokenType.LT, "<", startLine, startCol)
            c == '>' -> if (match('=')) CToken(CTokenType.GT_EQ, ">=", startLine, startCol) else CToken(CTokenType.GT, ">", startLine, startCol)
            c == '&' -> if (match('&')) CToken(CTokenType.AMP_AMP, "&&", startLine, startCol) else CToken(CTokenType.BIT_AND, "&", startLine, startCol)
            c == '|' -> if (match('|')) CToken(CTokenType.PIPE_PIPE, "||", startLine, startCol) else CToken(CTokenType.BIT_OR, "|", startLine, startCol)
            c == '^' -> CToken(CTokenType.BIT_XOR, "^", startLine, startCol)
            c == '~' -> CToken(CTokenType.BIT_NOT, "~", startLine, startCol)
            c == '(' -> CToken(CTokenType.LPAREN, "(", startLine, startCol)
            c == ')' -> CToken(CTokenType.RPAREN, ")", startLine, startCol)
            c == '{' -> CToken(CTokenType.LBRACE, "{", startLine, startCol)
            c == '}' -> CToken(CTokenType.RBRACE, "}", startLine, startCol)
            c == '[' -> CToken(CTokenType.LBRACKET, "[", startLine, startCol)
            c == ']' -> CToken(CTokenType.RBRACKET, "]", startLine, startCol)
            c == ';' -> CToken(CTokenType.SEMICOLON, ";", startLine, startCol)
            c == ',' -> CToken(CTokenType.COMMA, ",", startLine, startCol)
            c == '?' -> CToken(CTokenType.QUESTION, "?", startLine, startCol)
            c == ':' -> CToken(CTokenType.COLON, ":", startLine, startCol)
            else -> throw LexerException("Unexpected character '$c' at line $startLine, col $startCol")
        }
    }

    private fun readNumber(first: Char, startLine: Int, startCol: Int): CToken {
        val sb = StringBuilder()
        sb.append(first)
        while (pos < source.length && peek().isDigit()) {
            sb.append(advance())
        }
        return CToken(CTokenType.NUMBER, sb.toString(), startLine, startCol)
    }

    private fun readIdentifierOrKeyword(first: Char, startLine: Int, startCol: Int): CToken {
        val sb = StringBuilder()
        sb.append(first)
        while (pos < source.length && (peek().isLetterOrDigit() || peek() == '_')) {
            sb.append(advance())
        }
        val text = sb.toString()
        val type = keywords[text] ?: CTokenType.IDENTIFIER
        return CToken(type, text, startLine, startCol)
    }
}
