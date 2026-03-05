package com.hiperbou.vm.ccompiler.parser

/**
 * Operator precedence levels for the Pratt expression parser.
 * Higher values bind more tightly.
 */
object CPrecedence {
    const val NONE       = 0
    const val ASSIGNMENT = 1   // =, +=, -=, *=, /=
    const val OR         = 2   // ||
    const val AND        = 3   // &&
    const val BIT_OR     = 4   // |
    const val BIT_XOR    = 5   // ^
    const val BIT_AND    = 6   // &
    const val EQUALITY   = 7   // ==, !=
    const val COMPARISON = 8   // <, >, <=, >=
    const val SUM        = 9   // +, -
    const val PRODUCT    = 10  // *, /, %
    const val PREFIX     = 11  // unary -, !, ~
    const val POSTFIX    = 12  // ++, --  (postfix)
    const val CALL       = 13  // func(args)
}
