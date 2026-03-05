package com.hiperbou.vm.ccompiler.lexer

enum class CTokenType {
    // Literals
    NUMBER,         // integer literal, e.g. 42

    // Identifiers and keywords
    IDENTIFIER,     // user-defined name
    INT,            // keyword: int
    RETURN,         // keyword: return
    IF,             // keyword: if
    ELSE,           // keyword: else
    WHILE,          // keyword: while
    FOR,            // keyword: for
    DO,             // keyword: do (explicit block scope)

    // Built-in functions
    PRINT,          // built-in: print

    // Arithmetic operators
    PLUS,           // +
    MINUS,          // -
    STAR,           // *
    SLASH,          // /
    PERCENT,        // %

    // Comparison operators
    EQ_EQ,          // ==
    BANG_EQ,        // !=
    LT,             // <
    GT,             // >
    LT_EQ,          // <=
    GT_EQ,          // >=

    // Logical operators
    AMP_AMP,        // &&
    PIPE_PIPE,      // ||
    BANG,           // !

    // Assignment operators
    EQ,             // =
    PLUS_EQ,        // +=
    MINUS_EQ,       // -=
    STAR_EQ,        // *=
    SLASH_EQ,       // /=

    // Increment / decrement
    PLUS_PLUS,      // ++
    MINUS_MINUS,    // --

    // Punctuation
    LPAREN,         // (
    RPAREN,         // )
    LBRACE,         // {
    RBRACE,         // }
    SEMICOLON,      // ;
    COMMA,          // ,

    // End of input
    EOF
}

