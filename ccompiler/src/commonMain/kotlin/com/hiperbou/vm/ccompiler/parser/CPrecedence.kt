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
    const val EQUALITY   = 4   // ==, !=
    const val COMPARISON = 5   // <, >, <=, >=
    const val SUM        = 6   // +, -
    const val PRODUCT    = 7   // *, /, %
    const val PREFIX     = 8   // unary -, !
    const val POSTFIX    = 9   // ++, --  (postfix)
    const val CALL       = 10  // func(args)
}
