package com.hiperbou.vm.minicvm.ast

import com.hiperbou.vm.minicvm.lexer.Token

interface AstNode

// Expression Nodes
interface ExpressionNode : AstNode

data class NumberLiteralNode(val value: Int) : ExpressionNode
data class VariableAccessNode(val name: String) : ExpressionNode
data class ArrayAccessNode(val arrayName: String, val indexExpression: ExpressionNode) : ExpressionNode
data class BinaryOpNode(val left: ExpressionNode, val operator: Token, val right: ExpressionNode) : ExpressionNode
data class UnaryOpNode(val operator: Token, val operand: ExpressionNode) : ExpressionNode
data class FunctionCallNode(val functionName: String, val arguments: List<ExpressionNode>) : ExpressionNode
data class TernaryOpNode(val condition: ExpressionNode, val thenExpr: ExpressionNode, val elseExpr: ExpressionNode) : ExpressionNode
data class UpdateExpressionNode(val operatorToken: Token, val operand: ExpressionNode, val isPrefix: Boolean) : ExpressionNode

// Statement Nodes
interface StatementNode : ExpressionNode

data class VariableDeclarationNode(
    val name: String,
    val type: String, // "int"
    val initializer: ExpressionNode?
) : StatementNode

data class ArrayDeclarationNode(
    val name: String,
    val type: String, // "int"
    val size: ExpressionNode
) : StatementNode

data class AssignmentNode(
    val lvalue: ExpressionNode, // VariableAccessNode or ArrayAccessNode
    val expression: ExpressionNode
) : StatementNode

data class IfStatementNode(
    val condition: ExpressionNode,
    val thenBranch: BlockNode,
    val elseBranch: BlockNode?
) : StatementNode

data class WhileLoopNode(
    val condition: ExpressionNode,
    val body: BlockNode
) : StatementNode

data class ReturnStatementNode(val expression: ExpressionNode?) : StatementNode

data class ExpressionStatementNode(val expression: ExpressionNode) : StatementNode // For calls like foo();

data class DoWhileLoopNode(val body: BlockNode, val condition: ExpressionNode) : StatementNode

data class ForLoopNode(
    val initializer: StatementNode?,
    val condition: ExpressionNode?,
    val incrementer: ExpressionNode?,
    val body: BlockNode
) : StatementNode

object BreakNode : StatementNode // Using object for singleton as it has no state
object ContinueNode : StatementNode // Using object for singleton as it has no state


// Top-Level Nodes
interface TopLevelNode : AstNode // Can be FunctionDefinitionNode or GlobalVariableDeclarationNode

data class FunctionDefinitionNode(
    val name: String,
    val parameters: List<ParameterNode>,
    val returnType: String, // "int" or "void"
    val body: BlockNode
) : TopLevelNode

data class ParameterNode(val name: String, val type: String) : AstNode // type "int"

data class BlockNode(val statements: List<StatementNode>) : AstNode

// Program Node - Root of the AST
data class ProgramNode(val declarations: List<TopLevelNode>) : AstNode
