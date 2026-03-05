package com.hiperbou.vm.ccompiler.parser

import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.lexer.CToken
import com.hiperbou.vm.ccompiler.lexer.CTokenType

/**
 * Recursive descent parser for the C-like language.
 */
class CParser(tokens: List<CToken>) {

    private val stream = TokenStream(tokens)
    private val exprParser = CExpressionParser(stream)

    fun parse(): AstNode.Program {
        val globals = mutableListOf<AstNode.GlobalVarDecl>()
        val functions = mutableListOf<AstNode.FunctionDecl>()

        while (!stream.isAtEnd()) {
            stream.expect(CTokenType.INT)
            val nameToken = stream.expect(CTokenType.IDENTIFIER)

            if (stream.check(CTokenType.LPAREN)) {
                functions.add(parseFunctionDeclAfterName(nameToken.text))
            } else {
                globals.add(parseGlobalVarDeclAfterName(nameToken.text))
            }
        }

        return AstNode.Program(functions, globals)
    }

    private fun parseFunctionDeclAfterName(name: String): AstNode.FunctionDecl {
        stream.expect(CTokenType.LPAREN)
        val params = parseParamList()
        stream.expect(CTokenType.RPAREN)
        val body = parseBlock()
        return AstNode.FunctionDecl(name, params, body)
    }

    private fun parseGlobalVarDeclAfterName(name: String): AstNode.GlobalVarDecl {
        val initializer = if (stream.match(CTokenType.EQ)) exprParser.parseExpression() else null
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.GlobalVarDecl(name, initializer)
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

    private fun parseBlock(): AstNode.Block {
        stream.expect(CTokenType.LBRACE)
        val statements = mutableListOf<AstNode.Statement>()
        while (!stream.check(CTokenType.RBRACE) && !stream.isAtEnd()) {
            statements.add(parseStatement())
        }
        stream.expect(CTokenType.RBRACE)
        return AstNode.Block(statements)
    }

    private fun parseStatement(): AstNode.Statement {
        return when {
            stream.check(CTokenType.INT) -> parseVarDecl()
            stream.check(CTokenType.PRINT) -> parsePrintStatement()
            stream.check(CTokenType.RETURN) -> parseReturnStatement()
            stream.check(CTokenType.IF) -> parseIfStatement()
            stream.check(CTokenType.WHILE) -> parseWhileStatement()
            stream.check(CTokenType.FOR) -> parseForStatement()
            stream.check(CTokenType.DO) -> parseDoStatement()
            else -> parseExpressionStatement()
        }
    }

    private fun parseVarDecl(): AstNode.VarDecl {
        stream.expect(CTokenType.INT)
        val name = stream.expect(CTokenType.IDENTIFIER).text
        val initializer = if (stream.match(CTokenType.EQ)) exprParser.parseExpression() else null
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.VarDecl(name, initializer)
    }

    private fun parsePrintStatement(): AstNode.PrintStatement {
        stream.expect(CTokenType.PRINT)
        stream.expect(CTokenType.LPAREN)
        val expr = exprParser.parseExpression()
        stream.expect(CTokenType.RPAREN)
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.PrintStatement(expr)
    }

    private fun parseReturnStatement(): AstNode.ReturnStatement {
        stream.expect(CTokenType.RETURN)
        val expr = exprParser.parseExpression()
        stream.expect(CTokenType.SEMICOLON)
        return AstNode.ReturnStatement(expr)
    }

    private fun parseIfStatement(): AstNode.IfStatement {
        stream.expect(CTokenType.IF)
        stream.expect(CTokenType.LPAREN)
        val cond = exprParser.parseExpression()
        stream.expect(CTokenType.RPAREN)
        val thenBlock = parseBlock()
        val elseBlock = if (stream.match(CTokenType.ELSE)) parseBlock() else null
        return AstNode.IfStatement(cond, thenBlock, elseBlock)
    }

    private fun parseWhileStatement(): AstNode.WhileStatement {
        stream.expect(CTokenType.WHILE)
        stream.expect(CTokenType.LPAREN)
        val cond = exprParser.parseExpression()
        stream.expect(CTokenType.RPAREN)
        val body = parseBlock()
        return AstNode.WhileStatement(cond, body)
    }

    private fun parseForStatement(): AstNode.ForStatement {
        stream.expect(CTokenType.FOR)
        stream.expect(CTokenType.LPAREN)

        val init: AstNode.Statement? = when {
            stream.check(CTokenType.INT) -> {
                stream.consume()
                val name = stream.expect(CTokenType.IDENTIFIER).text
                val initializer = if (stream.match(CTokenType.EQ)) exprParser.parseExpression() else null
                AstNode.VarDecl(name, initializer)
            }
            stream.check(CTokenType.SEMICOLON) -> null
            else -> exprToStatement(exprParser.parseExpression())
        }
        stream.expect(CTokenType.SEMICOLON)

        val cond: AstNode.Expression? = if (!stream.check(CTokenType.SEMICOLON)) exprParser.parseExpression() else null
        stream.expect(CTokenType.SEMICOLON)

        val update: AstNode.Statement? = if (!stream.check(CTokenType.RPAREN)) {
            exprToStatement(exprParser.parseExpression())
        } else null
        stream.expect(CTokenType.RPAREN)

        val body = parseBlock()
        return AstNode.ForStatement(init, cond, update, body)
    }

    private fun parseDoStatement(): AstNode.DoStatement {
        stream.expect(CTokenType.DO)
        return AstNode.DoStatement(parseBlock())
    }

    private fun parseExpressionStatement(): AstNode.Statement {
        val expr = exprParser.parseExpression()
        stream.expect(CTokenType.SEMICOLON)
        return exprToStatement(expr)
    }

    private fun exprToStatement(expr: AstNode.Expression): AstNode.Statement = when (expr) {
        is AstNode.AssignExpr -> AstNode.AssignStatement(expr.name, expr.value)
        is AstNode.CompoundAssignExpr -> AstNode.CompoundAssign(expr.name, expr.op, expr.value)
        else -> AstNode.ExpressionStatement(expr)
    }
}
