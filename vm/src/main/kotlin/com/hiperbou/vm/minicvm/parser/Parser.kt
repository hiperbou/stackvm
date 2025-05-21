package com.hiperbou.vm.minicvm.parser

import com.hiperbou.vm.minicvm.ast.*
import com.hiperbou.vm.minicvm.lexer.Token
import com.hiperbou.vm.minicvm.lexer.TokenType
import com.hiperbou.vm.minicvm.lexer.TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): ProgramNode {
        val declarations = mutableListOf<TopLevelNode>()
        while (!isAtEnd() && peek().type != EOF) {
            declarations.add(parseTopLevelDeclaration())
        }
        return ProgramNode(declarations)
    }

    private fun parseTopLevelDeclaration(): TopLevelNode {
        // For now, only function definitions are top-level.
        // Global variable declarations could be added here.
        return parseFunctionDefinition()
    }

    private fun parseFunctionDefinition(): FunctionDefinitionNode {
        val returnType = consume(listOf(INT, VOID), "Expected 'int' or 'void' for return type.").lexeme
        val name = consume(IDENTIFIER, "Expected function name.").lexeme
        consume(LPAREN, "Expected '(' after function name.")
        val parameters = parseParameters()
        consume(RPAREN, "Expected ')' after parameters.")
        val body = parseBlock()
        return FunctionDefinitionNode(name, parameters, returnType, body)
    }

    private fun parseParameters(): List<ParameterNode> {
        val parameters = mutableListOf<ParameterNode>()
        if (peek().type != RPAREN) {
            do {
                if (parameters.isNotEmpty()) {
                    consume(COMMA, "Expected ',' between parameters.")
                }
                val paramType = consume(INT, "Expected 'int' for parameter type.").lexeme // Only int for now
                val paramName = consume(IDENTIFIER, "Expected parameter name.").lexeme
                parameters.add(ParameterNode(paramName, paramType))
            } while (peek().type == COMMA)
        }
        return parameters
    }

    private fun parseBlock(): BlockNode {
        consume(LBRACE, "Expected '{' to start a block.")
        val statements = mutableListOf<StatementNode>()
        while (peek().type != RBRACE && !isAtEnd()) {
            statements.add(parseStatement())
        }
        consume(RBRACE, "Expected '}' to end a block.")
        return BlockNode(statements)
    }

    private fun parseStatement(): StatementNode {
        return when (peek().type) {
            INT -> parseVariableOrArrayDeclaration()
            IF -> parseIfStatement()
            WHILE -> parseWhileLoop()
            RETURN -> parseReturnStatement()
            IDENTIFIER -> {
                // Could be an assignment or a function call statement
                // Lookahead to see if it's a function call (IDENTIFIER LPAREN) or assignment (IDENTIFIER ASSIGN or IDENTIFIER LBRACKET ... ASSIGN)
                if (peekNext().type == LPAREN || peekNext().type == ASSIGN || peekNext().type == LBRACKET) {
                     val expr = parseAssignmentOrFunctionCallOrArrayAssignment() // This will be an expression
                     consume(SEMICOLON, "Expected ';' after expression statement.")
                     // If it was an assignment, it's fine. If it was just a function call, wrap it.
                     // The parseAssignmentOrFunctionCallOrArrayAssignment() returns the expression itself.
                     // If it's a function call, it should be wrapped in ExpressionStatementNode
                     // If it's an assignment, it's an AssignmentNode, which is a StatementNode
                     if (expr is AssignmentNode) {
                         return expr // It's already a statement
                     } else if (expr is FunctionCallNode) {
                         return ExpressionStatementNode(expr)
                     }
                     // This case should ideally be handled better by parseAssignmentOrFunctionCallOrArrayAssignment
                     // For now, assume if it's not an assignment, it's an expression statement (e.g. function call)
                     return ExpressionStatementNode(expr)
                } else {
                    throw ParserException("Invalid statement starting with identifier: ${peek().lexeme} at line ${peek().line}")
                }
            }
            else -> {
                 // Try parsing an expression statement (e.g. a function call)
                val expr = parseExpression()
                consume(SEMICOLON, "Expected ';' after expression statement.")
                return ExpressionStatementNode(expr)
            }
        }
    }


    private fun parseVariableOrArrayDeclaration(): StatementNode {
        consume(INT, "Expected 'int' keyword.") // Assuming only int type for now
        val name = consume(IDENTIFIER, "Expected variable name.").lexeme
        return if (match(LBRACKET)) {
            val size = parseExpression()
            consume(RBRACKET, "Expected ']' after array size.")
            consume(SEMICOLON, "Expected ';' after array declaration.")
            ArrayDeclarationNode(name, "int", size)
        } else if (match(ASSIGN)) {
            val initializer = parseExpression()
            consume(SEMICOLON, "Expected ';' after variable declaration.")
            VariableDeclarationNode(name, "int", initializer)
        } else {
            consume(SEMICOLON, "Expected ';' after variable declaration.")
            VariableDeclarationNode(name, "int", null)
        }
    }

    private fun parseIfStatement(): IfStatementNode {
        consume(IF, "Expected 'if'.")
        consume(LPAREN, "Expected '(' after 'if'.")
        val condition = parseExpression()
        consume(RPAREN, "Expected ')' after if condition.")
        val thenBranch = parseBlock()
        var elseBranch: BlockNode? = null
        if (match(ELSE)) {
            elseBranch = parseBlock()
        }
        return IfStatementNode(condition, thenBranch, elseBranch)
    }

    private fun parseWhileLoop(): WhileLoopNode {
        consume(WHILE, "Expected 'while'.")
        consume(LPAREN, "Expected '(' after 'while'.")
        val condition = parseExpression()
        consume(RPAREN, "Expected ')' after while condition.")
        val body = parseBlock()
        return WhileLoopNode(condition, body)
    }

    private fun parseReturnStatement(): ReturnStatementNode {
        consume(RETURN, "Expected 'return'.")
        var expression: ExpressionNode? = null
        if (peek().type != SEMICOLON) {
            expression = parseExpression()
        }
        consume(SEMICOLON, "Expected ';' after return statement.")
        return ReturnStatementNode(expression)
    }

    // This function handles assignments, function calls (as expressions), and array assignments.
    // It's called when an IDENTIFIER is encountered in a context where an expression or assignment is expected.
    private fun parseAssignmentOrFunctionCallOrArrayAssignment(): ExpressionNode {
        val identifierToken = consume(IDENTIFIER, "Expected identifier.")
        val identifierName = identifierToken.lexeme

        if (match(LPAREN)) { // Function call
            val arguments = parseArguments()
            consume(RPAREN, "Expected ')' after function arguments.")
            var expr: ExpressionNode = FunctionCallNode(identifierName, arguments)
             // Check for chained operations like func_call() + 1, but assignment like func_call() = 1 is not allowed
            if (isBinaryOperator(peek().type) && peek().type != ASSIGN) {
                 expr = parseBinaryOpRHS(0, expr) // Precedence for RHS
            }
            return expr
        } else if (match(LBRACKET)) { // Array access or assignment
            val indexExpression = parseExpression()
            consume(RBRACKET, "Expected ']' after array index.")
            val arrayAccess = ArrayAccessNode(identifierName, indexExpression)
            if (match(ASSIGN)) {
                val valueExpression = parseExpression()
                return AssignmentNode(arrayAccess, valueExpression)
            }
            // It's just an array access, treat it as a primary expression that might be part of a larger expression
            return parseBinaryOpRHS(0, arrayAccess) // Continue parsing if it's part of a binary op
        } else if (match(ASSIGN)) { // Variable assignment
            val valueExpression = parseExpression()
            return AssignmentNode(VariableAccessNode(identifierName), valueExpression)
        } else {
            // It's just a variable access, treat it as a primary expression
            // It might be the start of a binary operation
            return parseBinaryOpRHS(0, VariableAccessNode(identifierName))
        }
    }


    // Expression parsing using precedence climbing
    private fun parseExpression(): ExpressionNode {
        return parseAssignment() // Assignment has the lowest precedence for expressions that can be statements
    }

    private fun parseAssignment(): ExpressionNode {
        val expr = parseLogicalOr() // Next higher precedence

        if (match(ASSIGN)) {
            val equals = previous()
            val value = parseAssignment() // Right-associative
            if (expr is VariableAccessNode || expr is ArrayAccessNode) {
                return AssignmentNode(expr, value)
            }
            throw ParserException("Invalid assignment target at line ${equals.line}.")
        }
        return expr
    }

    private fun parseLogicalOr(): ExpressionNode {
        var expr = parseLogicalAnd()
        while (match(LOGICAL_OR)) {
            val operator = previous()
            val right = parseLogicalAnd()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseLogicalAnd(): ExpressionNode {
        var expr = parseEquality()
        while (match(LOGICAL_AND)) {
            val operator = previous()
            val right = parseEquality()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseEquality(): ExpressionNode {
        var expr = parseComparison()
        while (match(EQ, NEQ)) {
            val operator = previous()
            val right = parseComparison()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseComparison(): ExpressionNode {
        var expr = parseTerm()
        while (match(LT, GT, LTE, GTE)) {
            val operator = previous()
            val right = parseTerm()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseTerm(): ExpressionNode {
        var expr = parseFactor()
        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = parseFactor()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseFactor(): ExpressionNode {
        var expr = parseUnary()
        while (match(MULTIPLY, DIVIDE, MODULO)) {
            val operator = previous()
            val right = parseUnary()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseUnary(): ExpressionNode {
        if (match(LOGICAL_NOT, MINUS)) {
            val operator = previous()
            val right = parseUnary() // Unary operators are often right-associative
            return UnaryOpNode(operator, right)
        }
        return parsePrimary()
    }

    private fun parsePrimary(): ExpressionNode {
        return when {
            match(INTEGER_LITERAL) -> NumberLiteralNode(previous().lexeme.toInt())
            match(TRUE) -> NumberLiteralNode(1) // Represent true as 1
            match(FALSE) -> NumberLiteralNode(0) // Represent false as 0
            match(IDENTIFIER) -> {
                val identifierToken = previous()
                val name = identifierToken.lexeme
                if (match(LPAREN)) { // Function call
                    val arguments = parseArguments()
                    consume(RPAREN, "Expected ')' after function arguments.")
                    FunctionCallNode(name, arguments)
                } else if (match(LBRACKET)) { // Array access
                    val index = parseExpression()
                    consume(RBRACKET, "Expected ']' after array index.")
                    ArrayAccessNode(name, index)
                } else { // Variable access
                    VariableAccessNode(name)
                }
            }
            match(LPAREN) -> {
                val expr = parseExpression()
                consume(RPAREN, "Expected ')' after expression.")
                expr
            }
            else -> throw ParserException("Expected expression, found ${peek().type} at line ${peek().line}")
        }
    }

     private fun parseBinaryOpRHS(exprPrecedence: Int, lhs: ExpressionNode): ExpressionNode {
        var currentLHS = lhs
        while (true) {
            val currentToken = peek()
            val tokenPrecedence = getTokenPrecedence(currentToken.type)
            if (tokenPrecedence < exprPrecedence || tokenPrecedence == -1) { // -1 if not an operator
                break
            }

            val operator = advance() // Consume operator
            var rhs = parsePrimary() // Parse primary for RHS, then handle higher precedence ops for RHS

            val nextPrecedence = getTokenPrecedence(peek().type)
            if (tokenPrecedence < nextPrecedence) { // Right-associativity or higher precedence on right
                 rhs = parseBinaryOpRHS(tokenPrecedence + if (isRightAssociative(operator.type)) 0 else 1, rhs)
            }
            currentLHS = BinaryOpNode(currentLHS, operator, rhs)
        }
        return currentLHS
    }

    private fun getTokenPrecedence(type: TokenType): Int {
        return when (type) {
            ASSIGN -> 1 // Lowest precedence for assignment in expression context (though handled separately by parseAssignment)
            LOGICAL_OR -> 2
            LOGICAL_AND -> 3
            EQ, NEQ -> 4
            LT, GT, LTE, GTE -> 5
            PLUS, MINUS -> 6
            MULTIPLY, DIVIDE, MODULO -> 7
            // Unary operators would be higher, but parseUnary handles them before binary.
            else -> -1 // Not a binary operator or not applicable here
        }
    }

    private fun isBinaryOperator(type: TokenType): Boolean {
        return getTokenPrecedence(type) > 0 || type == ASSIGN
    }

    private fun isRightAssociative(type: TokenType): Boolean {
        return type == ASSIGN // Only assignment is typically right-associative
    }


    private fun parseArguments(): List<ExpressionNode> {
        val arguments = mutableListOf<ExpressionNode>()
        if (peek().type != RPAREN) {
            do {
                if (arguments.isNotEmpty()) {
                    consume(COMMA, "Expected ',' between arguments.")
                }
                arguments.add(parseExpression())
            } while (peek().type == COMMA)
        }
        return arguments
    }

    // Utility functions
    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw error(peek(), message)
    }

    private fun consume(types: List<TokenType>, message: String): Token {
        for (type in types) {
            if (check(type)) return advance()
        }
        throw error(peek(), message)
    }


    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = current >= tokens.size || peek().type == EOF

    private fun peek(): Token = tokens[current]

    private fun previous(): Token = tokens[current - 1]

    private fun peekNext(): Token = if (current + 1 >= tokens.size) tokens.last() else tokens[current+1]


    private fun error(token: Token, message: String): ParserException {
        // In a real compiler, you might pass this to an error reporter
        return ParserException("Error at token ${token.lexeme} (line ${token.line}): $message")
    }
}

class ParserException(message: String) : RuntimeException(message)
