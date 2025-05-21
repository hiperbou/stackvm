package com.hiperbou.vm.minicvm.lexer

enum class TokenType {
    // Keywords
    INT, VOID, IF, ELSE, WHILE, RETURN, TRUE, FALSE,
    DO, FOR, BREAK, CONTINUE, // New keywords

    // Identifiers
    IDENTIFIER,

    // Literals
    INTEGER_LITERAL,

    // Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN,
    EQ, NEQ, LT, GT, LTE, GTE,
    LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT,
    BITWISE_AND, BITWISE_OR, BITWISE_XOR, BITWISE_NOT, // New bitwise operators
    INCREMENT, DECREMENT, // New increment/decrement operators

    // Punctuation
    SEMICOLON, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA,
    QMARK, COLON, // New punctuation for ternary operator

    // End of File
    EOF
}
