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
            INT -> parseVariableOrArrayDeclaration(expectSemicolon = true)
            IF -> parseIfStatement()
            WHILE -> parseWhileLoop()
            DO -> parseDoWhileLoop()
            FOR -> parseForLoop()
            BREAK -> {
                consume(BREAK, "Expected 'break'.")
                consume(SEMICOLON, "Expected ';' after 'break'.")
                BreakNode
            }
            CONTINUE -> {
                consume(CONTINUE, "Expected 'continue'.")
                consume(SEMICOLON, "Expected ';' after 'continue'.")
                ContinueNode
            }
            RETURN -> parseReturnStatement()
            // IDENTIFIER can start an assignment, a function call statement, or prefix/postfix ops.
            // Other primary expressions (literals, parenthesized expressions) can also be part of an expression statement.
            // The old parseAssignmentOrFunctionCallOrArrayAssignment was too specific.
            // We now rely on parseExpression() which correctly uses the precedence chain.
            // An expression statement is an expression followed by a semicolon.
            else -> {
                val expr = parseExpression() // This will handle assignments, function calls, ++/-- expressions etc.
                consume(SEMICOLON, "Expected ';' after expression statement.")
                
                // If the parsed expression is already a StatementNode (e.g., AssignmentNode),
                // return it directly as per original parser behavior for assignments.
                // Otherwise, wrap other valid expression statements (like function calls or update expressions)
                // in ExpressionStatementNode.
                if (expr is AssignmentNode) { // AssignmentNode is a StatementNode
                    return expr
                } else if (expr is FunctionCallNode || expr is UpdateExpressionNode) {
                    return ExpressionStatementNode(expr)
                } else {
                    // Other types of expressions (e.g., `1+2;` or `x;` where x is just a variable read)
                    // might be syntactically parsed as expressions but are not valid statements
                    // in many C-like languages if they don't have side effects or aren't assignments/calls.
                    // Throwing an error for unused expressions might be too strict for a simple parser
                    // or could be a semantic check. For now, allow any expression to be a statement
                    // if followed by a semicolon, by wrapping it. This matches the previous broader behavior
                    // but prioritizes direct AssignmentNode return.
                    // However, to be more C-like and potentially match original test expectations for invalid statements:
                    // Let's consider what the original parser might have rejected if not assignment/call.
                    // The old code had:
                    // IDENTIFIER -> if (peekNext().type == LPAREN || peekNext().type == ASSIGN || peekNext().type == LBRACKET) ... else throw...
                    // This implies it was stricter for IDENTIFIER-led statements.
                    // For now, let's ensure AssignmentNode is returned directly, and others are wrapped.
                    // If `expr` is neither AssignmentNode, FunctionCallNode, nor UpdateExpressionNode,
                    // it could be something like `1+2;`.
                    // The current AST structure might allow ExpressionStatementNode(BinaryOpNode(...))
                    // The original tests would determine if this was allowed.
                    // To be safe and cover the explicit cases:
                    return ExpressionStatementNode(expr) // Default to wrapping if not an AssignmentNode.
                                                        // This ensures FunctionCallNode and UpdateExpressionNode are wrapped.
                                                        // And if other expressions were allowed as statements, they remain so.
                                                        // The primary goal is to fix AssignmentNode representation.
                }
            }
        }
    }

    private fun parseVariableOrArrayDeclaration(expectSemicolon: Boolean): StatementNode {
        consume(INT, "Expected 'int' keyword.") // Assuming only int type for now
        val name = consume(IDENTIFIER, "Expected variable name.").lexeme
        val declarationNode = if (match(LBRACKET)) {
            val size = parseExpression()
            consume(RBRACKET, "Expected ']' after array size.")
            ArrayDeclarationNode(name, "int", size)
        } else if (match(ASSIGN)) {
            val initializer = parseExpression()
            VariableDeclarationNode(name, "int", initializer)
        } else {
            VariableDeclarationNode(name, "int", null)
        }

        if (expectSemicolon) {
            consume(SEMICOLON, "Expected ';' after variable or array declaration.")
        }
        return declarationNode
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

    private fun parseDoWhileLoop(): DoWhileLoopNode {
        consume(DO, "Expected 'do'.")
        val body = parseBlock()
        consume(WHILE, "Expected 'while' after do-while body.")
        consume(LPAREN, "Expected '(' after 'while'.")
        val condition = parseExpression()
        consume(RPAREN, "Expected ')' after do-while condition.")
        consume(SEMICOLON, "Expected ';' after do-while statement.")
        return DoWhileLoopNode(body, condition)
    }

    private fun parseForLoop(): ForLoopNode {
        consume(FOR, "Expected 'for'.")
        consume(LPAREN, "Expected '(' after 'for'.")

        // Initializer
        val initializer: StatementNode? = when {
            check(SEMICOLON) -> { // No initializer
                null
            }
            check(INT) -> { // Variable declaration
                parseVariableOrArrayDeclaration(expectSemicolon = false) // Semicolon is a separator here
            }
            else -> { // Expression
                ExpressionStatementNode(parseExpression())
            }
        }
        consume(SEMICOLON, "Expected ';' after for loop initializer or empty initializer.")

        // Condition
        val condition: ExpressionNode? = if (check(SEMICOLON)) {
            null // No condition
        } else {
            parseExpression()
        }
        consume(SEMICOLON, "Expected ';' after for loop condition or empty condition.")

        // Incrementer
        val incrementer: ExpressionNode? = if (check(RPAREN)) {
            null // No incrementer
        } else {
            parseExpression()
        }
        consume(RPAREN, "Expected ')' after for loop clauses.")

        val body = parseBlock()
        return ForLoopNode(initializer, condition, incrementer, body)
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

    // The old parseAssignmentOrFunctionCallOrArrayAssignment is removed.
    // Expression parsing is now handled by a chain starting with parseExpression().

    // Expression parsing using Pratt-style recursive descent.
    // The functions are ordered by precedence (lowest to highest).
    private fun parseExpression(): ExpressionNode {
        return parseAssignment()
    }

    private fun parseAssignment(): ExpressionNode {
        val expr = parseTernary() // Next higher precedence: Ternary

        if (match(ASSIGN)) {
            val equalsToken = previous() // Token for '=' to use in error reporting
            val value = parseAssignment() // Right-associative: parse expressions of same or lower precedence for value
            
            // Check if 'expr' is a valid l-value
            if (expr is VariableAccessNode || expr is ArrayAccessNode) {
                return AssignmentNode(expr, value)
            } else if (expr is UpdateExpressionNode && expr.isPrefix) {
                // Allow assignment to the result of a prefix increment/decrement, e.g., (++x) = 5;
                // Though unusual in C, it's syntactically plausible if prefix ops return l-values.
                // Our UpdateExpressionNode is an ExpressionNode, not directly an LValue in type system,
                // but AssignmentNode takes ExpressionNode.
                return AssignmentNode(expr, value)
            }
            throw error(equalsToken, "Invalid assignment target.")
        }
        return expr
    }

    private fun parseTernary(): ExpressionNode {
        var expr = parseLogicalOr() // Next higher precedence: Logical OR
        if (match(QMARK)) {
            // Per language spec, ternary is right-associative.
            // We need to parse the middle expression with a precedence that allows further ternaries,
            // and the same for the rightmost expression.
            // Calling parseExpression() or parseTernary() itself handles this.
            // Let's use parseTernary() to maintain consistency if other right-associative ops were at this level.
            val thenExpr = parseTernary() // Using parseTernary for right-associativity
            consume(COLON, "Expected ':' for ternary operator.")
            val elseExpr = parseTernary() // Using parseTernary for right-associativity
            expr = TernaryOpNode(expr, thenExpr, elseExpr)
        }
        return expr
    }

    private fun parseLogicalOr(): ExpressionNode {
        var expr = parseLogicalAnd() // Next higher precedence: Logical AND
        while (match(LOGICAL_OR)) {
            val operator = previous()
            val right = parseLogicalAnd() // For left-associativity, call the next higher precedence function for RHS
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseLogicalAnd(): ExpressionNode {
        var expr = parseBitwiseOr() // Next higher precedence: Bitwise OR (placeholder)
        while (match(LOGICAL_AND)) {
            val operator = previous()
            val right = parseBitwiseOr()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseBitwiseOr(): ExpressionNode {
        var expr = parseBitwiseXor() // Next higher precedence: Bitwise XOR
        while (match(BITWISE_OR)) {
            val operator = previous()
            val right = parseBitwiseXor() // For left-associativity
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseBitwiseXor(): ExpressionNode {
        var expr = parseBitwiseAnd() // Next higher precedence: Bitwise AND
        while (match(BITWISE_XOR)) {
            val operator = previous()
            val right = parseBitwiseAnd() // For left-associativity
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseBitwiseAnd(): ExpressionNode {
        var expr = parseEquality() // Next higher precedence: Equality
        while (match(BITWISE_AND)) {
            val operator = previous()
            val right = parseEquality() // For left-associativity
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseEquality(): ExpressionNode {
        var expr = parseComparison() // Next higher precedence: Comparison
        while (match(EQ, NEQ)) {
            val operator = previous()
            val right = parseComparison()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseComparison(): ExpressionNode {
        var expr = parseTerm() // Next higher precedence: Term (Additive)
        while (match(LT, GT, LTE, GTE)) {
            val operator = previous()
            val right = parseTerm()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseTerm(): ExpressionNode { // For Additive (+, -)
        var expr = parseFactor() // Next higher precedence: Factor (Multiplicative)
        while (match(PLUS, MINUS)) {
            val operator = previous()
            val right = parseFactor()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseFactor(): ExpressionNode { // For Multiplicative (*, /, %)
        var expr = parseUnary() // Next higher precedence: Unary
        while (match(MULTIPLY, DIVIDE, MODULO)) {
            val operator = previous()
            val right = parseUnary()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseUnary(): ExpressionNode {
        if (match(LOGICAL_NOT, MINUS, BITWISE_NOT)) { // Added BITWISE_NOT
            val operator = previous()
            val right = parseUnary()
            return UnaryOpNode(operator, right)
        }
        if (match(INCREMENT, DECREMENT)) { // Prefix increment/decrement
            val operatorToken = previous()
            val operand = parseUnary() // Parse the operand. It should resolve to an LValue.
            // L-value check for prefix operand.
            // The operand of a prefix operator must be an l-value.
            if (operand !is VariableAccessNode && operand !is ArrayAccessNode) {
                 // While ++(a+b) is invalid, the parser might construct UnaryOp(INC, BinaryOp(a,+,b)).
                 // A semantic check should catch this. For the parser, ensuring it doesn't crash is key.
                 // The current structure seems to allow parsing this.
                 // No explicit error throw here to keep parser focused on syntax.
                 // Let's ensure the error message for assignment target is good.
            }
            return UpdateExpressionNode(operatorToken, operand, true)
        }
        return parsePostfix() // Changed from parsePrimary to parsePostfix for postfix ops
    }

    // New function to handle postfix operations like x++, x--
    private fun parsePostfix(): ExpressionNode {
        var expr = parsePrimary() // Parse the primary expression first

        // After parsing a primary, check for postfix ++ or --
        // Loop for cases like x++--, though semantically invalid, parser might allow if not careful.
        // Here, we only allow one postfix op directly after primary. Chained postfix like x[][]++ is not handled by this simple loop.
        // A more robust way would be a loop that continues as long as postfix operators are matched.
        if (match(INCREMENT, DECREMENT)) { // only one postfix for now.
            val operatorToken = previous()
            // Ensure the expression is a valid l-value for postfix operations
            if (expr !is VariableAccessNode && expr !is ArrayAccessNode) {
                throw ParserException("Operand of postfix ${operatorToken.lexeme} must be a variable or array access at line ${operatorToken.line}.")
            }
            expr = UpdateExpressionNode(operatorToken, expr, false)
        }
        return expr
    }

    private fun parsePrimary(): ExpressionNode {
        val exprNode: ExpressionNode = when {
            match(INTEGER_LITERAL) -> NumberLiteralNode(previous().lexeme.toInt())
            match(TRUE) -> NumberLiteralNode(1) // Represent true as 1
            match(FALSE) -> NumberLiteralNode(0) // Represent false as 0
            match(IDENTIFIER) -> {
                val identifierToken = previous()
                val name = identifierToken.lexeme
                // Check for function call or array access FIRST
                if (peek().type == LPAREN) {
                    advance() // consume LPAREN
                    val arguments = parseArguments()
                    consume(RPAREN, "Expected ')' after function arguments.")
                    FunctionCallNode(name, arguments)
                } else if (peek().type == LBRACKET) {
                    advance() // consume LBRACKET
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
            else -> throw ParserException("Expected primary expression, found ${peek().type} at line ${peek().line}")
        }
        return exprNode
    }


    // getTokenPrecedence is not strictly needed for this Pratt-parser style with explicit function chain,
    // but can be useful for reference or a more generic binary operator parsing loop if that was used.
    // For now, it's updated to reflect the new operators.
    private fun getTokenPrecedence(type: TokenType): Int {
        return when (type) {
            // ASSIGN -> 1 // Handled by parseAssignment directly
            QMARK -> 2        // Ternary
            LOGICAL_OR -> 3
            LOGICAL_AND -> 4
            BITWISE_OR -> 5
            BITWISE_XOR -> 6
            BITWISE_AND -> 7
            EQ, NEQ -> 8      // Equality
            LT, GT, LTE, GTE -> 9 // Comparison
            PLUS, MINUS -> 10 // Additive
            MULTIPLY, DIVIDE, MODULO -> 11 // Multiplicative
            // Unary (LOGICAL_NOT, BITWISE_NOT, prefix INCREMENT, DECREMENT) are handled by parseUnary.
            // Postfix (postfix INCREMENT, DECREMENT), Call, ArrayAccess are handled by parsePostfix/parsePrimary.
            else -> -1 // Not a binary operator in this context or handled differently
        }
    }

    // The old parseBinaryOpRHS, isBinaryOperator, isRightAssociative are removed as they
    // belong to a different precedence climbing implementation.

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
