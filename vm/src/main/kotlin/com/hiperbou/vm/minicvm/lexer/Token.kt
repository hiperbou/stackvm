package com.hiperbou.vm.minicvm.lexer

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int
)

enum class TokenType {
    // Keywords
    INT, VOID, IF, ELSE, WHILE, RETURN, TRUE, FALSE, DO, FOR, BREAK, CONTINUE, CONST,

    // Identifiers
    IDENTIFIER,

    // Literals
    INTEGER_LITERAL,

    // Operators
    PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    ASSIGN,
    EQ, NEQ, LT, GT, LTE, GTE,
    LOGICAL_AND, LOGICAL_OR, LOGICAL_NOT, // !, &&, ||
    BITWISE_AND,    // &
    BITWISE_OR,     // |
    BITWISE_XOR,    // ^
    BITWISE_NOT,    // ~
    INCREMENT,      // ++
    DECREMENT,      // --
    QUESTION_MARK,  // ?
    COLON,          // :


    // Punctuation
    SEMICOLON, LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, COMMA,

    // End of File
    EOF
}
