package com.hiperbou.vm.ccompiler.parser

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.lexer.CToken
import com.hiperbou.vm.ccompiler.lexer.CTokenType

/**
 * Recursive descent parser for the C-like language.
 *
 * Handles top-level declarations (function definitions) and statements.
 * Delegates expression parsing to [CExpressionParser] (Pratt parser).
 *
 * Grammar (simplified, Phase 1–7):
 *
 * ```
 * program        → functionDecl*
 * functionDecl   → 'int' IDENTIFIER '(' paramList? ')' block
 * paramList      → 'int' IDENTIFIER (',' 'int' IDENTIFIER)*
 * block          → '{' statement* '}'
 * statement      → varDecl
 *                | printStmt
 *                | returnStmt
 *                | ifStmt
 *                | whileStmt
 *                | forStmt
 *                | exprStmt
 * varDecl        → 'int' IDENTIFIER ('=' expression)? ';'
 * printStmt      → 'print' '(' expression ')' ';'
 * returnStmt     → 'return' expression ';'
 * ifStmt         → 'if' '(' expression ')' block ('else' block)?
 * whileStmt      → 'while' '(' expression ')' block
 * forStmt        → 'for' '(' forInit? ';' expression? ';' forUpdate? ')' block
 * forInit        → varDecl | assignStmt (without trailing ';')
 * forUpdate      → expression (without trailing ';')
 * exprStmt       → expression ';'
 * ```
 */
class CParser(tokens: List<CToken>) {

    private val stream = TokenStream(tokens)
    private val exprParser = CExpressionParser(stream)

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    fun parse(): AstNode.Program {
        val functions = mutableListOf<AstNode.FunctionDecl>()
        while (!stream.isAtEnd()) {
            functions.add(parseFunctionDecl())
        }
        return AstNode.Program(functions)
    }

    // -------------------------------------------------------------------------
    // Function declaration
    // -------------------------------------------------------------------------

    private fun parseFunctionDecl(): AstNode.FunctionDecl {
        stream.expect(CTokenType.INT)
        val name = stream.expect(CTokenType.IDENTIFIER).text
        stream.expect(CTokenType.LPAREN)
        val params = parseParamList()
        stream.expect(CTokenType.RPAREN)
        val body = parseBlock()
        return AstNode.FunctionDecl(name, params, body)
    }

    private fun parseParamList(): List<AstNode.Param> {
        val params = mutableListOf<AstNode.Param>()
        if (stream.check(CTokenType.RPAREN)) return params
        do {
            stream.expect(CTokenType.INT)
            val paramName = stream.expect(CTokenType.IDENTIFIER).text
            params.add(AstNode.Param(paramName))
        } while (stream.match(CTokenType.COMMA))
        return params
    }

    // -------------------------------------------------------------------------
    // Block
    // -------------------------------------------------------------------------

    private fun parseBlock(): AstNode.Block {
        stream.expect(CTokenType.LBRACE)
        val statements = mutableListOf<AstNode.Statement>()
        while (!stream.check(CTokenType.RBRACE) && !stream.isAtEnd()) {
            statements.add(parseStatement())
        }
        stream.expect(CTokenType.RBRACE)
        return AstNode.Block(statements)
    }

    // -------------------------------------------------------------------------
    // Statements
    // -------------------------------------------------------------------------

    private fun parseStatement(): AstNode.Statement {
        return when {
            stream.check(CTokenType.INT)    -> parseVarDecl()
            stream.check(CTokenType.PRINT)  -> parsePrintStatement()
            stream.check(CTokenType.RETURN) -> parseReturnStatement()
            stream.check(CTokenType.IF)     -> parseIfStatement()
            stream.check(CTokenType.WHILE)  -> parseWhileStatement()
            stream.check(CTokenType.FOR)    -> parseForStatement()
            else                            -> parseExpressionStatement()
        }
    }

    /** `int x = expr;` or `int x;` */
    private fun parseVarDecl(): AstNode.VarDecl {
        stream.expect(CTokenType.INT)
        val name = stream.expect(CTokenType.IDENTIFIER).text
        val initializer = if (stream.match(CTokenType.EQ)) exprParser.parseExpression() else null
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.VarDecl(name, initializer)
    }

    /** `print(expr);` */
    private fun parsePrintStatement(): AstNode.PrintStatement {
        stream.expect(CTokenType.PRINT)
        stream.expect(CTokenType.LPAREN)
        val expr = exprParser.parseExpression()
        stream.expect(CTokenType.RPAREN)
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.PrintStatement(expr)
    }

    /** `return expr;` */
    private fun parseReturnStatement(): AstNode.ReturnStatement {
        stream.expect(CTokenType.RETURN)
        val expr = exprParser.parseExpression()
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.ReturnStatement(expr)
    }

    /** `if (cond) block [else block]` */
    private fun parseIfStatement(): AstNode.IfStatement {
        stream.expect(CTokenType.IF)
        stream.expect(CTokenType.LPAREN)
        val cond = exprParser.parseExpression()
        stream.expect(CTokenType.RPAREN)
        val thenBlock = parseBlock()
        val elseBlock = if (stream.match(CTokenType.ELSE)) parseBlock() else null
        return AstNode.IfStatement(cond, thenBlock, elseBlock)
    }

    /** `while (cond) block` */
    private fun parseWhileStatement(): AstNode.WhileStatement {
        stream.expect(CTokenType.WHILE)
        stream.expect(CTokenType.LPAREN)
        val cond = exprParser.parseExpression()
        stream.expect(CTokenType.RPAREN)
        val body = parseBlock()
        return AstNode.WhileStatement(cond, body)
    }

    /** `for (init?; cond?; update?) block` */
    private fun parseForStatement(): AstNode.ForStatement {
        stream.expect(CTokenType.FOR)
        stream.expect(CTokenType.LPAREN)

        // init: either `int x = expr` (no semicolon yet) or an expression, or empty
        val init: AstNode.Statement? = when {
            stream.check(CTokenType.INT) -> {
                stream.consume() // consume 'int'
                val name = stream.expect(CTokenType.IDENTIFIER).text
                val initializer = if (stream.match(CTokenType.EQ)) exprParser.parseExpression() else null
                // semicolon consumed below
                AstNode.VarDecl(name, initializer)
            }
            stream.check(CTokenType.SEMICOLON) -> null
            else -> {
                val expr = exprParser.parseExpression()
                exprToStatement(expr)
            }
        }
        stream.expect(CTokenType.SEMICOLON)

        val cond: AstNode.Expression? = if (!stream.check(CTokenType.SEMICOLON)) exprParser.parseExpression() else null
        stream.expect(CTokenType.SEMICOLON)

        val update: AstNode.Statement? = if (!stream.check(CTokenType.RPAREN)) {
            val expr = exprParser.parseExpression()
            exprToStatement(expr)
        } else null
        stream.expect(CTokenType.RPAREN)

        val body = parseBlock()
        return AstNode.ForStatement(init, cond, update, body)
    }

    /** `expr;` — expression used as a statement (e.g. `i++`, `x = 5`, `foo()`) */
    private fun parseExpressionStatement(): AstNode.Statement {
        val expr = exprParser.parseExpression()
        stream.expect(CTokenType.SEMICOLON)
        return exprToStatement(expr)
    }

    /**
     * Convert an expression to a statement.
     * [AstNode.AssignExpr] → [AstNode.AssignStatement]
     * [AstNode.CompoundAssignExpr] → [AstNode.CompoundAssign]
     * Everything else → [AstNode.ExpressionStatement]
     */
    private fun exprToStatement(expr: AstNode.Expression): AstNode.Statement = when (expr) {
        is AstNode.AssignExpr        -> AstNode.AssignStatement(expr.name, expr.value)
        is AstNode.CompoundAssignExpr -> AstNode.CompoundAssign(expr.name, expr.op, expr.value)
        else                          -> AstNode.ExpressionStatement(expr)
    }
}
