package com.hiperbou.vm.ccompiler.lexer

enum class CTokenType {
    NUMBER,

    IDENTIFIER,
    INT,
    RETURN,
    IF,
    ELSE,
    WHILE,
    FOR,
    DO,
    BREAK,
    CONTINUE,

    PRINT,

    PLUS,
    MINUS,
    STAR,
    SLASH,
    PERCENT,

    EQ_EQ,
    BANG_EQ,
    LT,
    GT,
    LT_EQ,
    GT_EQ,

    AMP_AMP,
    PIPE_PIPE,
    BANG,

    EQ,
    PLUS_EQ,
    MINUS_EQ,
    STAR_EQ,
    SLASH_EQ,

    PLUS_PLUS,
    MINUS_MINUS,

    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    SEMICOLON,
    COMMA,

    EOF
}
