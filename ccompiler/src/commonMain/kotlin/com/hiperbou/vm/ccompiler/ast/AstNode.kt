package com.hiperbou.vm.ccompiler.ast

sealed class AstNode {

    data class Program(
        val functions: List<FunctionDecl>,
        val globals: List<GlobalVarDecl> = emptyList()
    ) : AstNode()

    data class GlobalVarDecl(val name: String, val initializer: Expression?) : AstNode()

    data class FunctionDecl(
        val name: String,
        val params: List<Param>,
        val body: Block
    ) : AstNode()

    data class Param(val name: String) : AstNode()

    sealed class Statement : AstNode()

    data class Block(val statements: List<Statement>) : AstNode()
    data class VarDecl(val name: String, val initializer: Expression?) : Statement()
    data class AssignStatement(val name: String, val value: Expression) : Statement()
    data class CompoundAssign(val name: String, val op: BinaryOperator, val value: Expression) : Statement()
    data class PrintStatement(val expr: Expression) : Statement()
    data class DebugPrintStatement(val expr: Expression) : Statement()
    data class ReturnStatement(val expr: Expression) : Statement()
    data class IfStatement(val condition: Expression, val thenBlock: Block, val elseBlock: Block?) : Statement()
    data class WhileStatement(val condition: Expression, val body: Block) : Statement()
    data class DoStatement(val body: Block) : Statement()
    data class DoWhileStatement(val body: Block, val condition: Expression) : Statement()
    data class BreakStatement(val token: String = "break") : Statement()
    data class ContinueStatement(val token: String = "continue") : Statement()
    data class ForStatement(val init: Statement?, val condition: Expression?, val update: Statement?, val body: Block) : Statement()
    data class ExpressionStatement(val expr: Expression) : Statement()

    sealed class Expression : AstNode()

    data class NumberLiteral(val value: Int) : Expression()
    data class Identifier(val name: String) : Expression()
    data class BinaryOp(val left: Expression, val op: BinaryOperator, val right: Expression) : Expression()
    data class AssignExpr(val name: String, val value: Expression) : Expression()
    data class CompoundAssignExpr(val name: String, val op: BinaryOperator, val value: Expression) : Expression()
    data class UnaryOp(val op: UnaryOperator, val expr: Expression) : Expression()
    data class FunctionCall(val name: String, val args: List<Expression>) : Expression()
    data class PreIncDec(val op: IncDecOperator, val name: String) : Expression()
    data class PostIncDec(val name: String, val op: IncDecOperator) : Expression()
}

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


