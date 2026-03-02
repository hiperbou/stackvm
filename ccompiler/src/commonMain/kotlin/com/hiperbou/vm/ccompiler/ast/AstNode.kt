package com.hiperbou.vm.ccompiler.ast

/**
 * Sealed class hierarchy for the C-like compiler AST.
 *
 * Nodes are organized into:
 *  - Top-level: [Program], [FunctionDecl], [Param]
 *  - Statements: [Statement] subtypes
 *  - Expressions: [Expression] subtypes
 *
 * Phase 1 uses: Program, FunctionDecl, Block, PrintStatement, ReturnStatement, NumberLiteral
 * Later phases add: VarDecl, AssignStatement, IfStatement, WhileStatement, ForStatement,
 *                   Identifier, BinaryOp, UnaryOp, FunctionCall
 */
sealed class AstNode {

    // -------------------------------------------------------------------------
    // Top-level
    // -------------------------------------------------------------------------

    /** The root of the AST: a list of function declarations. */
    data class Program(val functions: List<FunctionDecl>) : AstNode()

    /** A function declaration: `int name(params) { body }` */
    data class FunctionDecl(
        val name: String,
        val params: List<Param>,
        val body: Block
    ) : AstNode()

    /** A function parameter: `int name` */
    data class Param(val name: String) : AstNode()

    // -------------------------------------------------------------------------
    // Statements
    // -------------------------------------------------------------------------

    sealed class Statement : AstNode()

    /** A block of statements: `{ stmt1; stmt2; ... }` */
    data class Block(val statements: List<Statement>) : AstNode()

    /** Variable declaration with optional initializer: `int x = expr;` */
    data class VarDecl(val name: String, val initializer: Expression?) : Statement()

    /** Assignment statement: `x = expr;` */
    data class AssignStatement(val name: String, val value: Expression) : Statement()

    /** Compound assignment: `x += expr;`, `x -= expr;`, etc. */
    data class CompoundAssign(val name: String, val op: BinaryOperator, val value: Expression) : Statement()

    /** `print(expr);` — built-in print, pops value after printing */
    data class PrintStatement(val expr: Expression) : Statement()

    /** `return expr;` */
    data class ReturnStatement(val expr: Expression) : Statement()

    /** `if (cond) thenBlock [else elseBlock]` */
    data class IfStatement(
        val condition: Expression,
        val thenBlock: Block,
        val elseBlock: Block?
    ) : Statement()

    /** `while (cond) body` */
    data class WhileStatement(val condition: Expression, val body: Block) : Statement()

    /**
     * `for (init; cond; update) body`
     * [init] is a [VarDecl] or [AssignStatement] (or null)
     * [update] is a [Statement] (or null)
     */
    data class ForStatement(
        val init: Statement?,
        val condition: Expression?,
        val update: Statement?,
        val body: Block
    ) : Statement()

    /** A standalone expression used as a statement (e.g. `i++;`) */
    data class ExpressionStatement(val expr: Expression) : Statement()

    // -------------------------------------------------------------------------
    // Expressions
    // -------------------------------------------------------------------------

    sealed class Expression : AstNode()

    /** Integer literal: `42` */
    data class NumberLiteral(val value: Int) : Expression()

    /** Variable reference: `x` */
    data class Identifier(val name: String) : Expression()

    /** Binary operation: `left op right` */
    data class BinaryOp(
        val left: Expression,
        val op: BinaryOperator,
        val right: Expression
    ) : Expression()

    /**
     * Assignment expression: `name = expr`
     * Produced by the Pratt parser when `=` appears in expression context.
     * Converted to [AstNode.AssignStatement] by [com.hiperbou.vm.ccompiler.parser.CParser].
     */
    data class AssignExpr(val name: String, val value: Expression) : Expression()

    /**
     * Compound assignment expression: `name op= expr`
     * Produced by the Pratt parser when `+=`, `-=`, etc. appear in expression context.
     * Converted to [AstNode.CompoundAssign] by [com.hiperbou.vm.ccompiler.parser.CParser].
     */
    data class CompoundAssignExpr(val name: String, val op: BinaryOperator, val value: Expression) : Expression()

    /** Unary operation: `op expr` */
    data class UnaryOp(val op: UnaryOperator, val expr: Expression) : Expression()

    /** Function call: `name(args...)` */
    data class FunctionCall(val name: String, val args: List<Expression>) : Expression()

    /** Pre-increment/decrement: `++x` / `--x` */
    data class PreIncDec(val op: IncDecOperator, val name: String) : Expression()

    /** Post-increment/decrement: `x++` / `x--` */
    data class PostIncDec(val name: String, val op: IncDecOperator) : Expression()
}

// -------------------------------------------------------------------------
// Operator enums
// -------------------------------------------------------------------------

enum class BinaryOperator(val symbol: String) {
    ADD("+"), SUB("-"), MUL("*"), DIV("/"), MOD("%"),
    EQ("=="), NE("!="), LT("<"), GT(">"), LTE("<="), GTE(">="),
    AND("&&"), OR("||")
}

enum class UnaryOperator(val symbol: String) {
    NEG("-"), NOT("!")
}

enum class IncDecOperator(val symbol: String) {
    INC("++"), DEC("--")
}
