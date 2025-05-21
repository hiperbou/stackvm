package com.hiperbou.vm.minicvm.lexer

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int
)

enum class TokenType {
    // Keywords
    INT, VOID, IF, ELSE, WHILE, RETURN, TRUE, FALSE,

    // Identifiers
    IDENTIFIER,

    // Literals
    INTEGER_LITERAL,

    // Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN,
    EQ, NEQ, LT, GT, LTE, GTE,
    LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT,

    // Punctuation
    SEMICOLON, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA,

    // End of File
    EOF
}
