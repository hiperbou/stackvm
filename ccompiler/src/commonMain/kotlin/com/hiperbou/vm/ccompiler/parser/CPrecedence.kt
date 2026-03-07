package com.hiperbou.vm.ccompiler.parser

/**
 * Operator precedence levels for the Pratt expression parser.
 * Higher values bind more tightly.
 */
object CPrecedence {
    const val NONE       = 0
    const val ASSIGNMENT = 1   // =, +=, -=, *=, /=
    const val TERNARY    = 2   // ?: (right-associative)
    const val OR         = 3   // ||
    const val AND        = 4   // &&
    const val BIT_OR     = 5   // |
    const val BIT_XOR    = 6   // ^
    const val BIT_AND    = 7   // &
    const val EQUALITY   = 8   // ==, !=
    const val COMPARISON = 9   // <, >, <=, >=
    const val SUM        = 10  // +, -
    const val PRODUCT    = 11  // *, /, %
    const val PREFIX     = 12  // unary -, !, ~
    const val POSTFIX    = 13  // ++, --  (postfix)
    const val CALL       = 14  // func(args)
}


