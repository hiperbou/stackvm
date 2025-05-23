package com.hiperbou.vm.minicvm.parser

import com.hiperbou.vm.minicvm.ast.*
import com.hiperbou.vm.minicvm.lexer.Token
import com.hiperbou.vm.minicvm.lexer.TokenType
import com.hiperbou.vm.minicvm.lexer.TokenType.*

class Parser(private val tokens: List<Token>) {
    private var current = 0
    private var loopDepth = 0 // For checking break/continue

    private fun enterLoop() { loopDepth++ }
    private fun exitLoop() { loopDepth-- }
    private fun isInLoop(): Boolean = loopDepth > 0

    fun parse(): ProgramNode {
        val declarations = mutableListOf<TopLevelNode>()
        while (!isAtEnd() && peek().type != EOF) {
            declarations.add(parseTopLevelDeclaration())
        }
        return ProgramNode(declarations)
    }

    private fun parseTopLevelDeclaration(): TopLevelNode {
        var isConst = false
        if (peek().type == CONST) {
            isConst = true
            consume(CONST, "Expected 'const'.")
            if (peek().type != INT) { // Semantic Check: 'const' must be followed by 'int' for variables/top-level
                throw ParserException("Expected 'int' after 'const' for top-level declaration at line ${peek().line}.")
            }
        }

        val typeToken = peek()

        if (typeToken.type == INT) {
            val isFunctionSyntax = tokens.getOrNull(current + 1)?.type == IDENTIFIER &&
                                   tokens.getOrNull(current + 2)?.type == LPAREN
            
            if (isFunctionSyntax) {
                if (isConst) {
                    throw ParserException("'const' keyword is not applicable to function definitions at line ${previous().line}.")
                }
                val funcReturnType = consume(INT, "Expected 'int' for function return type.").lexeme
                return parseFunctionDefinitionInternalRest(funcReturnType)
            } else { 
                val declarationNode = parseVariableOrArrayDeclarationInternal(isConst) 
                if (declarationNode is TopLevelNode) {
                    return declarationNode
                } else {
                    throw ParserException("Expected a top-level variable or array declaration but got ${declarationNode::class.simpleName} at line ${peek().line}")
                }
            }
        } else if (typeToken.type == VOID) {
            if (isConst) {
                throw ParserException("Cannot declare 'const void' at line ${typeToken.line}.")
            }
             if (tokens.getOrNull(current + 1)?.type == IDENTIFIER && tokens.getOrNull(current + 2)?.type == LPAREN) {
                val funcReturnType = consume(VOID, "Expected 'void' for function return type.").lexeme
                return parseFunctionDefinitionInternalRest(funcReturnType)
            } else {
                throw ParserException("Invalid 'void' declaration. 'void' can only be used for function return types. At line ${typeToken.line}")
            }
        }
        throw ParserException("Expected '[const] int' or 'void' for top-level declaration at line ${typeToken.line}")
    }
    
    private fun parseFunctionDefinitionInternalRest(returnTypeLexeme: String): FunctionDefinitionNode {
        val name = consume(IDENTIFIER, "Expected function name.").lexeme
        consume(LPAREN, "Expected '(' after function name.")
        val parameters = parseParameters()
        consume(RPAREN, "Expected ')' after parameters.")
        val body = parseBlock()
        return FunctionDefinitionNode(name, parameters, returnTypeLexeme, body)
    }

    private fun parseParameters(): List<ParameterNode> {
        val parameters = mutableListOf<ParameterNode>()
        if (peek().type != RPAREN) {
            do {
                if (parameters.isNotEmpty()) {
                    consume(COMMA, "Expected ',' between parameters.")
                }
                val paramType = consume(INT, "Expected 'int' for parameter type.").lexeme 
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
            CONST -> {
                consume(CONST, "Expected 'const'.")
                if (peek().type != INT) throw ParserException("Expected 'int' after 'const' for variable declaration at line ${peek().line}.")
                parseVariableOrArrayDeclarationInternal(isConst = true)
            }
            INT -> parseVariableOrArrayDeclarationInternal(isConst = false)
            IF -> parseIfStatement()
            WHILE -> parseWhileLoop()
            DO -> parseDoWhileStatement()
            FOR -> parseForStatement()
            BREAK -> parseBreakStatement()
            CONTINUE -> parseContinueStatement()
            RETURN -> parseReturnStatement()
            // IDENTIFIER can start an assignment, a function call, or an inc/dec statement
            // INCREMENT/DECREMENT can start a prefix inc/dec statement
            // LPAREN for parenthesized expressions like (x)++;
            // Other primary expressions that can be statements (e.g. function calls)
            IDENTIFIER, INCREMENT, DECREMENT, LPAREN, INTEGER_LITERAL, TRUE, FALSE, QUESTION_MARK -> { // Added QUESTION_MARK for ternary as statement
                val expr = parseExpression() 
                consume(SEMICOLON, "Expected ';' after expression statement.")
                return ExpressionStatementNode(expr)
            }
            else -> {
                throw ParserException("Unexpected token ${peek().type} at start of statement at line ${peek().line}")
            }
        }
    }

    private fun parseVariableOrArrayDeclarationInternal(isConst: Boolean): StatementNode {
        consume(INT, "Expected 'int' keyword.") 
        val nameToken = consume(IDENTIFIER, "Expected variable name.")
        val name = nameToken.lexeme

        if (match(LBRACKET)) { 
            if (isConst) { 
                throw ParserException("Arrays cannot be declared 'const': '$name' at line ${nameToken.line}.")
            }
            val size = parseExpression()
            consume(RBRACKET, "Expected ']' after array size.")
            consume(SEMICOLON, "Expected ';' after array declaration.")
            return ArrayDeclarationNode(name, "int", size)
        } else { 
            var initializer: ExpressionNode? = null
            if (match(ASSIGN)) {
                initializer = parseExpression()
            } else if (isConst) { 
                throw ParserException("Constant variable '$name' must be initialized at line ${nameToken.line}.")
            }

            if (isConst) {
                val isValidConstInitializer = when (val initExpr = initializer!!) { 
                    is NumberLiteralNode -> true
                    is UnaryOpNode -> initExpr.operator.type == MINUS && initExpr.operand is NumberLiteralNode
                    else -> false
                }
                if (!isValidConstInitializer) {
                     throw ParserException("Constant variable '$name' initializer must be an integer literal (e.g., 10 or -10) at line ${nameToken.line}.")
                }
            }
            consume(SEMICOLON, "Expected ';' after variable declaration.")
            return VariableDeclarationNode(name, "int", initializer, isConst)
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
        enterLoop()
        val body = parseBlock()
        exitLoop()
        return WhileLoopNode(condition, body)
    }

    private fun parseDoWhileStatement(): DoWhileLoopNode {
        consume(DO, "Expected 'do'.")
        enterLoop()
        val body = parseBlock()
        exitLoop()
        consume(WHILE, "Expected 'while' after do-while body.")
        consume(LPAREN, "Expected '(' after 'while'.")
        val condition = parseExpression()
        consume(RPAREN, "Expected ')' after while condition.")
        consume(SEMICOLON, "Expected ';' after do-while statement.")
        return DoWhileLoopNode(body, condition)
    }

    private fun parseForStatement(): ForLoopNode {
        consume(FOR, "Expected 'for'.")
        consume(LPAREN, "Expected '(' after 'for'.")

        val initializer: StatementNode? = when {
            peek().type == CONST -> {
                consume(CONST, "Expected 'const'.")
                if (peek().type != INT) throw ParserException("Expected 'int' after 'const' in for-loop initializer at line ${peek().line}.")
                parseVariableOrArrayDeclarationInternal(isConst = true) 
            }
            peek().type == INT -> parseVariableOrArrayDeclarationInternal(isConst = false) 
            peek().type != SEMICOLON -> ExpressionStatementNode(parseExpression()).also { consume(SEMICOLON, "Expected ';' after for-loop initializer expression.") }
            else -> { 
                consume(SEMICOLON, "Expected ';' for empty for-loop initializer.")
                null
            }
        }

        val condition: ExpressionNode? = if (peek().type != SEMICOLON) {
            parseExpression()
        } else {
            null
        }
        consume(SEMICOLON, "Expected ';' after for-loop condition.")

        val updater: StatementNode? = if (peek().type != RPAREN) {
             ExpressionStatementNode(parseExpression())
        } else {
            null
        }
        consume(RPAREN, "Expected ')' after for-loop clauses.")

        enterLoop()
        val body = parseBlock()
        exitLoop()

        return ForLoopNode(initializer, condition, updater, body)
    }

    private fun parseBreakStatement(): StatementNode {
        consume(BREAK, "Expected 'break'.")
        if (!isInLoop()) {
            throw ParserException("'break' statement outside of a loop at line ${previous().line}.")
        }
        consume(SEMICOLON, "Expected ';' after 'break'.")
        return BreakNode
    }

    private fun parseContinueStatement(): StatementNode {
        consume(CONTINUE, "Expected 'continue'.")
        if (!isInLoop()) {
            throw ParserException("'continue' statement outside of a loop at line ${previous().line}.")
        }
        consume(SEMICOLON, "Expected ';' after 'continue'.")
        return ContinueNode
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
    
    private fun parseExpression(): ExpressionNode {
        return parseAssignment() 
    }

    private fun parseAssignment(): ExpressionNode {
        val expr = parseTernaryExpression() // Changed from parseLogicalOr

        if (match(ASSIGN)) {
            val equals = previous()
            val value = parseAssignment() // Assignment is right-associative
            if (expr is VariableAccessNode || expr is ArrayAccessNode) {
                return AssignmentNode(expr, value)
            }
            if (expr is PrefixIncrementNode || expr is PostfixIncrementNode || expr is PrefixDecrementNode || expr is PostfixDecrementNode) {
                 throw ParserException("The result of an increment/decrement operation cannot be assigned to at line ${equals.line}.")
            }
            throw ParserException("Invalid assignment target at line ${equals.line}.")
        }
        return expr
    }

    private fun parseTernaryExpression(): ExpressionNode {
        var expr = parseLogicalOr()
        if (match(QUESTION_MARK)) {
            val trueExpr = parseExpression() // Middle part can be any expression, including another ternary
            consume(COLON, "Expected ':' in ternary expression.")
            // For right-associativity (e.g., a ? b : c ? d : e), the falseExpr should also be parseTernaryExpression
            val falseExpr = parseTernaryExpression() 
            expr = TernaryOpNode(expr, trueExpr, falseExpr)
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
        var expr = parseBitwiseOr() 
        while (match(LOGICAL_AND)) {
            val operator = previous()
            val right = parseBitwiseOr() 
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseBitwiseOr(): ExpressionNode {
        var expr = parseBitwiseXor()
        while (match(BITWISE_OR)) {
            val operator = previous()
            val right = parseBitwiseXor()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseBitwiseXor(): ExpressionNode {
        var expr = parseBitwiseAnd()
        while (match(BITWISE_XOR)) {
            val operator = previous()
            val right = parseBitwiseAnd()
            expr = BinaryOpNode(expr, operator, right)
        }
        return expr
    }

    private fun parseBitwiseAnd(): ExpressionNode {
        var expr = parseEquality() 
        while (match(BITWISE_AND)) {
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
        if (match(INCREMENT)) {
            val target = parseUnary() 
            if (target !is VariableAccessNode && target !is ArrayAccessNode) {
                throw ParserException("Operand of prefix '++' must be an lvalue at line ${previous().line}.")
            }
            return PrefixIncrementNode(target)
        }
        if (match(DECREMENT)) {
            val target = parseUnary()
            if (target !is VariableAccessNode && target !is ArrayAccessNode) {
                throw ParserException("Operand of prefix '--' must be an lvalue at line ${previous().line}.")
            }
            return PrefixDecrementNode(target)
        }
        if (match(LOGICAL_NOT, MINUS, BITWISE_NOT)) { 
            val operator = previous()
            val right = parseUnary() 
            return UnaryOpNode(operator, right)
        }
        return parsePostfixExpression() 
    }
    
    private fun parsePostfixExpression(): ExpressionNode {
        var expr = parsePrimary()

        while(true) {
            when {
                match(INCREMENT) -> {
                    if (expr !is VariableAccessNode && expr !is ArrayAccessNode) {
                        throw ParserException("Operand of postfix '++' must be an lvalue at line ${previous().line}.")
                    }
                    expr = PostfixIncrementNode(expr)
                }
                match(DECREMENT) -> {
                    if (expr !is VariableAccessNode && expr !is ArrayAccessNode) {
                        throw ParserException("Operand of postfix '--' must be an lvalue at line ${previous().line}.")
                    }
                    expr = PostfixDecrementNode(expr)
                }
                match(LPAREN) -> { 
                     if (expr !is VariableAccessNode) throw ParserException("Function call target must be an identifier at line ${previous().line}.")
                    val arguments = parseArguments()
                    consume(RPAREN, "Expected ')' after function arguments.")
                    expr = FunctionCallNode(expr.name, arguments)
                }
                match(LBRACKET) -> { 
                    val arrayName = when(expr) {
                        is VariableAccessNode -> expr.name
                        is ArrayAccessNode -> expr.arrayName 
                        else -> throw ParserException("Array access target must be an identifier or array element at line ${previous().line}.")
                    }
                    val index = parseExpression()
                    consume(RBRACKET, "Expected ']' after array index.")
                    expr = ArrayAccessNode(arrayName, index) 
                }
                else -> break
            }
        }
        return expr
    }

    private fun parsePrimary(): ExpressionNode {
        return when {
            match(INTEGER_LITERAL) -> NumberLiteralNode(previous().lexeme.toInt())
            match(TRUE) -> NumberLiteralNode(1) 
            match(FALSE) -> NumberLiteralNode(0) 
            match(IDENTIFIER) -> VariableAccessNode(previous().lexeme)
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
            if (tokenPrecedence < exprPrecedence || tokenPrecedence == -1) { 
                break
            }

            val operator = advance() 
            var rhs = parseUnary() 

            val nextPrecedence = getTokenPrecedence(peek().type)
            if (tokenPrecedence < nextPrecedence) { 
                 rhs = parseBinaryOpRHS(tokenPrecedence + if (isRightAssociative(operator.type)) 0 else 1, rhs)
            }
            currentLHS = BinaryOpNode(currentLHS, operator, rhs)
        }
        return currentLHS
    }

    private fun getTokenPrecedence(type: TokenType): Int {
        return when (type) {
            // Postfix ++, --, (), [] are highest (handled in parsePostfixExpression)
            // Prefix ++, --, !, ~, - (handled in parseUnary)
            // Multiplicative *, /, %
            MULTIPLY, DIVIDE, MODULO -> 10
            // Additive +, -
            PLUS, MINUS -> 9
            // Relational <, <=, >, >=
            LT, GT, LTE, GTE -> 8
            // Equality ==, !=
            EQ, NEQ -> 7
            // Bitwise AND &
            BITWISE_AND -> 6
            // Bitwise XOR ^
            BITWISE_XOR -> 5
            // Bitwise OR |
            BITWISE_OR -> 4
            // Logical AND &&
            LOGICAL_AND -> 3
            // Logical OR ||
            LOGICAL_OR -> 2
            // Ternary ?: (handled in parseTernaryExpression, precedence between logical OR and assignment)
            // Assignment =, +=, -= etc. (Right-associative)
            ASSIGN -> 1 
            // Comma , (Lowest) - Not used in this precedence table directly
            else -> -1 
        }
    }

    private fun isBinaryOperator(type: TokenType): Boolean {
        return getTokenPrecedence(type) > 0 || type == ASSIGN
    }

    private fun isRightAssociative(type: TokenType): Boolean {
        // Ternary is right-associative, handle in its parsing function
        return type == ASSIGN 
    }


    private fun parseArguments(): List<ExpressionNode> {
        val arguments = mutableListOf<ExpressionNode>()
        if (peek().type != RPAREN) {
            do {
                if (arguments.isNotEmpty()) {
                    consume(COMMA, "Expected ',' between parameters.")
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
