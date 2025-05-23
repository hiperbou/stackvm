package com.hiperbou.vm.minicvm.codegen

import com.hiperbou.vm.minicvm.ast.*
import com.hiperbou.vm.minicvm.lexer.TokenType

class SymbolTable(private val parent: SymbolTable? = null) {
    private val symbols = mutableMapOf<String, SymbolInfo>()
    private var currentLocalOffset = 0 
    private var currentGlobalOffset = 0 
    private var currentArgOffset = 0 

    sealed class SymbolInfo(val name: String, val isGlobal: Boolean)
    class VariableInfo(
        name: String, 
        val address: Int, 
        val isArgument: Boolean = false, 
        val type: String = "int", 
        isGlobal: Boolean = false,
        val isConst: Boolean = false, 
        val constValue: Int? = null   
    ) : SymbolInfo(name, isGlobal)
    class ArrayInfo(name: String, val baseAddress: Int, val size: Int, val type: String = "int", isGlobal: Boolean = false) : SymbolInfo(name, isGlobal)
    class FunctionInfo(name: String, val label: String, val parameterCount: Int, val returnType: String) : SymbolInfo(name, true)

    fun defineVariable(
        name: String, 
        type: String = "int", 
        isArgument: Boolean = false, 
        isConst: Boolean = false, 
        constValue: Int? = null
    ): VariableInfo { 
        if (symbols.containsKey(name) && !isArgument) throw CodeGenException("Variable '$name' already defined in this scope.")
        
        val addressOffset = if (isConst) {
            -1 
        } else if (isArgument) {
            currentArgOffset++
            currentLocalOffset++ 
        } else {
            currentLocalOffset++
        }
        val actualAddress = if (isConst) -1 else addressOffset -1

        val info = VariableInfo(name, actualAddress, isArgument, type, isGlobal = false, isConst = isConst, constValue = constValue)
        symbols[name] = info
        return info
    }

    fun defineGlobalVariable(
        name: String, 
        type: String = "int", 
        isConst: Boolean = false, 
        constValue: Int? = null
    ): VariableInfo {
        if (symbols.containsKey(name)) throw CodeGenException("Global variable '$name' already defined.")
        
        val address = if (isConst) {
            -1 
        } else {
            currentGlobalOffset++ 
            currentGlobalOffset -1
        }
        val info = VariableInfo(name, address, isArgument = false, type = type, isGlobal = true, isConst = isConst, constValue = constValue)
        symbols[name] = info
        return info
    }

    fun defineArray(name: String, size: Int, type: String = "int"): ArrayInfo { 
        if (symbols.containsKey(name)) throw CodeGenException("Array '$name' already defined in this scope.")
        val info = ArrayInfo(name, currentLocalOffset, size, type, isGlobal = false)
        symbols[name] = info
        currentLocalOffset += size 
        return info
    }

    fun defineGlobalArray(name: String, size: Int, type: String = "int"): ArrayInfo {
        if (symbols.containsKey(name)) throw CodeGenException("Global array '$name' already defined.")
        val info = ArrayInfo(name, currentGlobalOffset, size, type, isGlobal = true)
        symbols[name] = info
        currentGlobalOffset += size 
        return info
    }

    fun defineFunction(name: String, parameterCount: Int, returnType: String): FunctionInfo {
        val label = "${name}_label" 
        if (symbols.containsKey(name)) throw CodeGenException("Function '$name' already defined.")
        val info = FunctionInfo(name, label, parameterCount, returnType)
        symbols[name] = info
        return info
    }

    fun lookup(name: String): SymbolInfo? {
        return symbols[name] ?: parent?.lookup(name)
    }

    fun createChildScope(): SymbolTable {
        return SymbolTable(this)
    }

    fun resetLocalScope() { 
        if (parent == null) { 
            val persistentGlobals = symbols.filterValues { it.isGlobal } 
            symbols.clear()
            symbols.putAll(persistentGlobals)
        } else { 
            symbols.clear() 
        }
        currentLocalOffset = 0 
        currentArgOffset = 0   
    }

    fun trueClear() { 
        symbols.clear()
        currentLocalOffset = 0
        currentGlobalOffset = 0
        currentArgOffset = 0
    }
}

class CodeGenerator {
    private val assemblyCode = StringBuilder()
    private var labelCounter = 0
    private val globalSymbolTable = SymbolTable()
    private var currentFunctionSymbolTable: SymbolTable? = null
    private var currentFunctionName: String? = null

    private data class LoopContext(val continueLabel: String, val breakLabel: String)
    private val loopContextStack = ArrayDeque<LoopContext>()


    private fun newLabel(prefix: String = "L"): String {
        return "$prefix${labelCounter++}"
    }

    private fun emit(instruction: String) {
        assemblyCode.append(instruction).append("\n")
    }

    private fun emitLabel(label: String) {
        assemblyCode.append(label).append(":\n")
    }

    fun generate(programNode: ProgramNode): String {
        assemblyCode.clear()
        labelCounter = 0
        globalSymbolTable.trueClear() 

        programNode.declarations.forEach { declaration ->
            when (declaration) {
                is FunctionDefinitionNode -> {
                    globalSymbolTable.defineFunction(declaration.name, declaration.parameters.size, declaration.returnType)
                }
                is VariableDeclarationNode -> { 
                    if (declaration.isConst) {
                        val value = extractConstValue(declaration.initializer!!) 
                        globalSymbolTable.defineGlobalVariable(declaration.name, declaration.type, isConst = true, constValue = value)
                    } else {
                        globalSymbolTable.defineGlobalVariable(declaration.name, declaration.type, isConst = false, constValue = null)
                    }
                }
                is ArrayDeclarationNode -> { 
                    val size = (declaration.size as? NumberLiteralNode)?.value ?: throw CodeGenException("Global array size must be a number literal for now.")
                    globalSymbolTable.defineGlobalArray(declaration.name, size, declaration.type)
                }
            }
        }

        val mainFunctionInfo = globalSymbolTable.lookup("main") as? SymbolTable.FunctionInfo
        if (mainFunctionInfo != null) {
            emit("CALL ${mainFunctionInfo.label}")
            emit("HALT")
        } else if (programNode.declarations.any {it is FunctionDefinitionNode}) {
        }

        programNode.declarations.forEach { declaration ->
            visitTopLevelNode(declaration)
        }
        
        if (mainFunctionInfo == null && programNode.declarations.isNotEmpty()) {
            val lastInstruction = assemblyCode.lines().lastOrNull()?.trim()?.uppercase()
            if (lastInstruction != "HALT" && lastInstruction != "RET") { 
                 emit("HALT")
            }
        } else if (mainFunctionInfo == null && programNode.declarations.isEmpty()) {
            emit("HALT") 
        }

        return assemblyCode.toString()
    }

    private fun visitTopLevelNode(node: TopLevelNode) {
        when (node) {
            is FunctionDefinitionNode -> visitFunctionDefinition(node)
            is VariableDeclarationNode -> visitGlobalVariableDeclaration(node) 
            is ArrayDeclarationNode -> visitGlobalArrayDeclaration(node) 
            else -> throw CodeGenException("Unsupported top-level node: $node")
        }
    }

    private fun extractConstValue(expr: ExpressionNode): Int { 
        return when (expr) {
            is NumberLiteralNode -> expr.value
            is UnaryOpNode -> if (expr.operator.type == TokenType.MINUS && expr.operand is NumberLiteralNode) {
                -(expr.operand as NumberLiteralNode).value
            } else {
                throw CodeGenException("Invalid constant expression structure: Not a literal number or unary minus on literal number.")
            }
            else -> {
                throw CodeGenException("Invalid constant expression: Not a literal number.")
            }
        }
    }

    private fun visitGlobalVariableDeclaration(node: VariableDeclarationNode) {
        if (!node.isConst && node.initializer != null) {
            val varInfo = globalSymbolTable.lookup(node.name) as? SymbolTable.VariableInfo
                ?: throw CodeGenException("Global variable ${node.name} not found during code generation for initializer.") 
            visitExpression(node.initializer)
            emit("GSTORE ${varInfo.address}")
        }
    }

    private fun visitGlobalArrayDeclaration(node: ArrayDeclarationNode) {
    }

    private fun visitFunctionDefinition(node: FunctionDefinitionNode) {
        currentFunctionName = node.name
        val functionInfo = globalSymbolTable.lookup(node.name) as SymbolTable.FunctionInfo
        
        currentFunctionSymbolTable = globalSymbolTable.createChildScope() 

        emitLabel(functionInfo.label)
        node.parameters.forEachIndexed { index, param ->
            val paramInfo = currentFunctionSymbolTable!!.defineVariable(
                name = param.name, 
                type = param.type, 
                isArgument = true, 
                isConst = false,  
                constValue = null
            )
             emit("STORE ${paramInfo.address}") 
        }

        visitBlock(node.body)

        if (node.returnType == "void" || assemblyCode.lines().lastOrNull()?.trim() != "RET") {
            if (node.returnType != "void" && assemblyCode.lines().lastOrNull()?.trim() != "RET") {
            }
            emit("RET")
        }
        currentFunctionSymbolTable = null 
        currentFunctionName = null
    }

    private fun visitBlock(node: BlockNode) {
        val parentScope = currentFunctionSymbolTable
        currentFunctionSymbolTable = currentFunctionSymbolTable?.createChildScope() ?: globalSymbolTable.createChildScope()
        node.statements.forEach { visitStatement(it) }
        currentFunctionSymbolTable = parentScope
    }

    private fun visitStatement(node: StatementNode) {
        when (node) {
            is VariableDeclarationNode -> visitLocalVariableDeclaration(node)
            is ArrayDeclarationNode -> visitLocalArrayDeclaration(node)
            is AssignmentNode -> visitAssignment(node)
            is IfStatementNode -> visitIfStatement(node)
            is WhileLoopNode -> visitWhileLoop(node)
            is DoWhileLoopNode -> visitDoWhileLoopNode(node)
            is ForLoopNode -> visitForLoopNode(node)
            is BreakNode -> visitBreakNode(node)
            is ContinueNode -> visitContinueNode(node)
            is PrefixIncrementNode -> visitPrefixIncrementNode(node, isStatementContext = true)
            is PostfixIncrementNode -> visitPostfixIncrementNode(node, isStatementContext = true)
            is PrefixDecrementNode -> visitPrefixDecrementNode(node, isStatementContext = true)
            is PostfixDecrementNode -> visitPostfixDecrementNode(node, isStatementContext = true)
            is ReturnStatementNode -> visitReturnStatement(node)
            is ExpressionStatementNode -> visitExpressionStatement(node)
            else -> throw CodeGenException("Unsupported statement node: $node")
        }
    }

    private fun visitLocalVariableDeclaration(node: VariableDeclarationNode) {
        val table = currentFunctionSymbolTable ?: throw CodeGenException("No function symbol table for local variable declaration.")
        
        if (node.isConst) {
            val value = extractConstValue(node.initializer!!) 
            table.defineVariable(node.name, node.type, isArgument = false, isConst = true, constValue = value)
        } else {
            val varInfo = table.defineVariable(node.name, node.type, isArgument = false, isConst = false)
            node.initializer?.let {
                visitExpression(it)
                emit("STORE ${varInfo.address}")
            }
        }
    }

    private fun visitLocalArrayDeclaration(node: ArrayDeclarationNode) {
        val table = currentFunctionSymbolTable ?: throw CodeGenException("No function symbol table for local array declaration.")
        val size = (node.size as? NumberLiteralNode)?.value ?: throw CodeGenException("Local array size must be a number literal.")
        table.defineArray(node.name, size, node.type) 
    }

    private fun visitAssignment(node: AssignmentNode) {
        visitExpression(node.expression) 

        when (val lvalue = node.lvalue) {
            is VariableAccessNode -> {
                val symbolInfo = (currentFunctionSymbolTable?.lookup(lvalue.name) ?: globalSymbolTable.lookup(lvalue.name))
                    ?: throw CodeGenException("Variable '${lvalue.name}' not found for assignment.")
                
                if (symbolInfo !is SymbolTable.VariableInfo) throw CodeGenException("'${lvalue.name}' is not a variable.")
                if (symbolInfo.isConst) throw CodeGenException("Cannot assign to constant variable '${lvalue.name}'.") 

                if (symbolInfo.isGlobal) {
                    emit("GSTORE ${symbolInfo.address}")
                } else {
                    emit("STORE ${symbolInfo.address}")
                }
            }
            is ArrayAccessNode -> { 
                val symbolInfo = (currentFunctionSymbolTable?.lookup(lvalue.arrayName) ?: globalSymbolTable.lookup(lvalue.arrayName))
                    ?: throw CodeGenException("Array ${lvalue.arrayName} not found for assignment.")

                if (symbolInfo !is SymbolTable.ArrayInfo) throw CodeGenException("${lvalue.arrayName} is not an array.")

                emit("PUSH ${symbolInfo.baseAddress}")
                visitExpression(lvalue.indexExpression)
                emit("ADD") 

                if (symbolInfo.isGlobal) {
                    emit("GSTOREI") 
                } else {
                    emit("STOREI")
                }
            }
            else -> throw CodeGenException("Unsupported lvalue for assignment: $lvalue")
        }
    }

    private fun visitIfStatement(node: IfStatementNode) {
        val elseLabel = newLabel("IF_ELSE")
        val endIfLabel = newLabel("IF_END")

        visitExpression(node.condition)
        emit("JIF ${if (node.elseBranch != null) elseLabel else endIfLabel}")

        visitBlock(node.thenBranch)
        if (node.elseBranch != null) {
            emit("JMP $endIfLabel")
            emitLabel(elseLabel)
            visitBlock(node.elseBranch)
        }
        emitLabel(endIfLabel)
    }

    private fun visitWhileLoop(node: WhileLoopNode) {
        val conditionLabel = newLabel("WHILE_COND")
        val endLoopLabel = newLabel("WHILE_END")

        loopContextStack.addLast(LoopContext(continueLabel = conditionLabel, breakLabel = endLoopLabel))

        emitLabel(conditionLabel)
        visitExpression(node.condition)
        emit("JIF $endLoopLabel") 

        visitBlock(node.body)
        emit("JMP $conditionLabel") 

        emitLabel(endLoopLabel)
        loopContextStack.removeLast()
    }

    private fun visitDoWhileLoopNode(node: DoWhileLoopNode) {
        val loopStartLabel = newLabel("DO_WHILE_START")
        val conditionLabel = newLabel("DO_WHILE_COND")
        val endLoopLabel = newLabel("DO_WHILE_END")

        loopContextStack.addLast(LoopContext(continueLabel = conditionLabel, breakLabel = endLoopLabel))

        emitLabel(loopStartLabel)
        visitBlock(node.body)

        emitLabel(conditionLabel)
        visitExpression(node.condition)
        emit("JIF $endLoopLabel")    
        emit("JMP $loopStartLabel")  

        emitLabel(endLoopLabel)
        loopContextStack.removeLast()
    }

    private fun visitForLoopNode(node: ForLoopNode) {
        val conditionLabel = newLabel("FOR_COND")
        val updaterLabel = newLabel("FOR_UPDATER")
        val endLoopLabel = newLabel("FOR_END")
        
        loopContextStack.addLast(LoopContext(continueLabel = updaterLabel, breakLabel = endLoopLabel))

        val parentScope = currentFunctionSymbolTable
        currentFunctionSymbolTable = currentFunctionSymbolTable?.createChildScope() ?: globalSymbolTable.createChildScope()

        node.initializer?.let {
            visitStatement(it)
        }

        emitLabel(conditionLabel) 

        if (node.condition != null) {
            visitExpression(node.condition)
            emit("JIF $endLoopLabel") 
        }
        
        visitBlock(node.body)

        emitLabel(updaterLabel)
        node.updater?.let {
            visitStatement(it) 
        }

        emit("JMP $conditionLabel")

        emitLabel(endLoopLabel)

        currentFunctionSymbolTable = parentScope 
        loopContextStack.removeLast()
    }

    private fun visitBreakNode(node: BreakNode) {
        if (loopContextStack.isEmpty()) {
            throw CodeGenException("Break statement outside of a loop.") 
        }
        emit("JMP ${loopContextStack.last().breakLabel}")
    }

    private fun visitContinueNode(node: ContinueNode) {
        if (loopContextStack.isEmpty()) {
            throw CodeGenException("Continue statement outside of a loop.") 
        }
        emit("JMP ${loopContextStack.last().continueLabel}")
    }

    private fun visitReturnStatement(node: ReturnStatementNode) {
        node.expression?.let {
            visitExpression(it)
        }
        emit("RET")
    }

    private fun visitExpressionStatement(node: ExpressionStatementNode) {
        visitExpression(node.expression)
        // If the expression is a function call that is NOT a built-in void function, pop its result.
        if (node.expression is FunctionCallNode) {
            val funcNode = node.expression
            if (funcNode.functionName != "print" && funcNode.functionName != "debug_print") {
                val funcInfo = globalSymbolTable.lookup(funcNode.functionName) as? SymbolTable.FunctionInfo
                if (funcInfo?.returnType != "void") {
                    emit("POP") 
                }
            }
        } else if (node.expression is PrefixIncrementNode || 
                   node.expression is PostfixIncrementNode || 
                   node.expression is PrefixDecrementNode || 
                   node.expression is PostfixDecrementNode) {
            // These built-in operations, when used as statements, might leave their result on stack.
            // Their respective visit methods with isStatementContext=true handle the POP.
            // However, visitExpressionStatement calls them with isStatementContext=false.
            // So, we need to pop here if they are the root of an expression statement.
            emit("POP")
        }
        // Other expressions as statements (like `a+b;`) would also leave a value.
        // For simplicity, we might assume such expressions are not typical or require explicit POP
        // if they are not one of the special cases above.
        // However, a general POP for non-function-call, non-inc/dec expressions might be safer
        // if the language allows `literal;` or `var;` as statements.
        // MiniCVM grammar for ExpressionStatementNode implies the expression is evaluated.
        // If it's not a function call or inc/dec, and it left a value, it should be popped.
        else if (node.expression !is FunctionCallNode && 
                 node.expression !is PrefixIncrementNode && node.expression !is PostfixIncrementNode &&
                 node.expression !is PrefixDecrementNode && node.expression !is PostfixDecrementNode) {
            // This case is tricky. If `5;` is a statement, PUSH 5 is emitted. It should be popped.
            // If `x;` is a statement, LOAD x is emitted. It should be popped.
            // This implies most expressions that aren't assignments or void function calls, when used as statements, need a POP.
            // The current inc/dec logic (when isStatementContext=false) *leaves* the value. So it needs a POP here.
            // Let's assume for now that any expression that is not a function call handled above needs a POP.
             emit("POP")
        }
    }

    private fun visitExpression(node: ExpressionNode) {
        when (node) {
            is NumberLiteralNode -> emit("PUSH ${node.value}")
            is VariableAccessNode -> {
                val symbolInfo = (currentFunctionSymbolTable?.lookup(node.name) ?: globalSymbolTable.lookup(node.name))
                    ?: throw CodeGenException("Variable '${node.name}' not found.")
                if (symbolInfo !is SymbolTable.VariableInfo) throw CodeGenException("'${node.name}' is not a variable.")

                if (symbolInfo.isConst) {
                    emit("PUSH ${symbolInfo.constValue!!}") 
                } else if (symbolInfo.isGlobal) {
                    emit("GLOAD ${symbolInfo.address}")
                } else {
                    emit("LOAD ${symbolInfo.address}")
                }
            }
            is ArrayAccessNode -> { 
                val symbolInfo = (currentFunctionSymbolTable?.lookup(node.arrayName) ?: globalSymbolTable.lookup(node.arrayName))
                    ?: throw CodeGenException("Array ${node.arrayName} not found.")
                if (symbolInfo !is SymbolTable.ArrayInfo) throw CodeGenException("${node.arrayName} is not an array.")

                emit("PUSH ${symbolInfo.baseAddress}")
                visitExpression(node.indexExpression)
                emit("ADD")
                if (symbolInfo.isGlobal) {
                    emit("GLOADI")
                } else {
                    emit("LOADI")
                }
            }
            is BinaryOpNode -> visitBinaryOp(node)
            is UnaryOpNode -> visitUnaryOp(node)
            is FunctionCallNode -> visitFunctionCall(node)
            is PrefixIncrementNode -> visitPrefixIncrementNode(node, isStatementContext = false)
            is PostfixIncrementNode -> visitPostfixIncrementNode(node, isStatementContext = false)
            is PrefixDecrementNode -> visitPrefixDecrementNode(node, isStatementContext = false)
            is PostfixDecrementNode -> visitPostfixDecrementNode(node, isStatementContext = false)
            is TernaryOpNode -> visitTernaryOpNode(node)
            else -> throw CodeGenException("Unsupported expression node: $node")
        }
    }

    private fun visitTernaryOpNode(node: TernaryOpNode) {
        val falseLabel = newLabel("TERNARY_FALSE")
        val endLabel = newLabel("TERNARY_END")

        visitExpression(node.condition) 
        emit("JIF $falseLabel")         

        visitExpression(node.trueExpression) 
        emit("JMP $endLabel")           

        emitLabel(falseLabel)
        visitExpression(node.falseExpression) 

        emitLabel(endLabel)
    }

    private fun lookupVariable(name: String): SymbolTable.VariableInfo {
        val symbol = (currentFunctionSymbolTable?.lookup(name) ?: globalSymbolTable.lookup(name))
            ?: throw CodeGenException("Variable '$name' not found.")
        if (symbol !is SymbolTable.VariableInfo) throw CodeGenException("'$name' is not a variable.")
        return symbol
    }

    private fun lookupArray(name: String): SymbolTable.ArrayInfo {
        val symbol = (currentFunctionSymbolTable?.lookup(name) ?: globalSymbolTable.lookup(name))
            ?: throw CodeGenException("Array '$name' not found.")
        if (symbol !is SymbolTable.ArrayInfo) throw CodeGenException("'$name' is not an array.")
        return symbol
    }

    private fun visitPrefixIncrementNode(node: PrefixIncrementNode, isStatementContext: Boolean) {
        when (val target = node.target) {
            is VariableAccessNode -> {
                val symbolInfo = lookupVariable(target.name)
                if (symbolInfo.isConst) throw CodeGenException("Cannot increment constant variable '${target.name}'.")
                val loadOp = if (symbolInfo.isGlobal) "GLOAD" else "LOAD"
                val storeOp = if (symbolInfo.isGlobal) "GSTORE" else "STORE"
                
                emit("$loadOp ${symbolInfo.address}") 
                emit("PUSH 1")
                emit("ADD")          
                emit("DUP")          
                emit("$storeOp ${symbolInfo.address}") 
                if (isStatementContext) emit("POP") 
            }
            is ArrayAccessNode -> {
                val arrayInfo = lookupArray(target.arrayName)
                val loadIOp = if (arrayInfo.isGlobal) "GLOADI" else "LOADI"
                val storeIOp = if (arrayInfo.isGlobal) "GSTOREI" else "STOREI"
                
                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(target.indexExpression)
                emit("ADD")          
                emit("DUP")          
                emit(loadIOp)        
                emit("PUSH 1")
                emit("ADD")          
                emit("DUP")          
                emit("SWAP") 
                emit(storeIOp)       
                if (isStatementContext) emit("POP")
            }
            else -> throw CodeGenException("Invalid target for prefix increment: $target")
        }
    }

    private fun visitPostfixIncrementNode(node: PostfixIncrementNode, isStatementContext: Boolean) {
        when (val target = node.target) {
            is VariableAccessNode -> {
                val symbolInfo = lookupVariable(target.name)
                if (symbolInfo.isConst) throw CodeGenException("Cannot increment constant variable '${target.name}'.")
                val loadOp = if (symbolInfo.isGlobal) "GLOAD" else "LOAD"
                val storeOp = if (symbolInfo.isGlobal) "GSTORE" else "STORE"

                emit("$loadOp ${symbolInfo.address}") 
                emit("DUP")          
                emit("PUSH 1")
                emit("ADD")          
                emit("$storeOp ${symbolInfo.address}") 
                if (isStatementContext) emit("POP") 
            }
            is ArrayAccessNode -> {
                val arrayInfo = lookupArray(target.arrayName)
                val loadIOp = if (arrayInfo.isGlobal) "GLOADI" else "LOADI"
                val storeIOp = if (arrayInfo.isGlobal) "GSTOREI" else "STOREI"

                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(target.indexExpression)
                emit("ADD")          
                emit("DUP")          
                emit(loadIOp)        
                emit("DUP")          
                emit("SWAP")         
                emit("PUSH 1")
                emit("ADD")          
                emit(storeIOp)       
                if (isStatementContext) emit("POP")
            }
            else -> throw CodeGenException("Invalid target for postfix increment: $target")
        }
    }
    
    private fun visitPrefixDecrementNode(node: PrefixDecrementNode, isStatementContext: Boolean) {
         when (val target = node.target) {
            is VariableAccessNode -> {
                val symbolInfo = lookupVariable(target.name)
                if (symbolInfo.isConst) throw CodeGenException("Cannot decrement constant variable '${target.name}'.")
                val loadOp = if (symbolInfo.isGlobal) "GLOAD" else "LOAD"
                val storeOp = if (symbolInfo.isGlobal) "GSTORE" else "STORE"
                
                emit("$loadOp ${symbolInfo.address}")
                emit("PUSH 1")
                emit("SUB")          
                emit("DUP")          
                emit("$storeOp ${symbolInfo.address}")
                if (isStatementContext) emit("POP")
            }
            is ArrayAccessNode -> {
                val arrayInfo = lookupArray(target.arrayName)
                val loadIOp = if (arrayInfo.isGlobal) "GLOADI" else "LOADI"
                val storeIOp = if (arrayInfo.isGlobal) "GSTOREI" else "STOREI"
                
                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(target.indexExpression)
                emit("ADD")          
                emit("DUP")          
                emit(loadIOp)        
                emit("PUSH 1")
                emit("SUB")          
                emit("DUP")          
                emit("SWAP")         
                emit(storeIOp)       
                if (isStatementContext) emit("POP")
            }
            else -> throw CodeGenException("Invalid target for prefix decrement: $target")
        }
    }

    private fun visitPostfixDecrementNode(node: PostfixDecrementNode, isStatementContext: Boolean) {
        when (val target = node.target) {
            is VariableAccessNode -> {
                val symbolInfo = lookupVariable(target.name)
                if (symbolInfo.isConst) throw CodeGenException("Cannot decrement constant variable '${target.name}'.")
                val loadOp = if (symbolInfo.isGlobal) "GLOAD" else "LOAD"
                val storeOp = if (symbolInfo.isGlobal) "GSTORE" else "STORE"

                emit("$loadOp ${symbolInfo.address}") 
                emit("DUP")          
                emit("PUSH 1")
                emit("SUB")          
                emit("$storeOp ${symbolInfo.address}") 
                 if (isStatementContext) emit("POP")
            }
            is ArrayAccessNode -> {
                val arrayInfo = lookupArray(target.arrayName)
                val loadIOp = if (arrayInfo.isGlobal) "GLOADI" else "LOADI"
                val storeIOp = if (arrayInfo.isGlobal) "GSTOREI" else "STOREI"

                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(target.indexExpression)
                emit("ADD")          
                emit("DUP")          
                emit(loadIOp)        
                emit("DUP")          
                emit("SWAP")         
                emit("PUSH 1")
                emit("SUB")          
                emit(storeIOp)       
                if (isStatementContext) emit("POP")
            }
            else -> throw CodeGenException("Invalid target for postfix decrement: $target")
        }
    }

    private fun visitBinaryOp(node: BinaryOpNode) {
        if (node.operator.type == TokenType.LOGICAL_AND) {
            val falseLabel = newLabel("AND_FALSE")
            val endLabel = newLabel("AND_END")
            visitExpression(node.left)
            emit("JIF $falseLabel") 
            visitExpression(node.right)
            emit("JIF $falseLabel") 
            emit("PUSH 1")          
            emit("JMP $endLabel")
            emitLabel(falseLabel)
            emit("PUSH 0")          
            emitLabel(endLabel)
            return
        } else if (node.operator.type == TokenType.LOGICAL_OR) {
            val endLabel = newLabel("OR_END")
            val nextCheckLabel = newLabel("OR_NEXT") 
            visitExpression(node.left)
            emit("JIF $nextCheckLabel") 
            emit("PUSH 1")            
            emit("JMP $endLabel")
            emitLabel(nextCheckLabel)
            visitExpression(node.right)
            val falseResultLabel = newLabel("OR_FALSE_RES")
            emit("JIF $falseResultLabel")
            emit("PUSH 1")
            emit("JMP $endLabel")
            emitLabel(falseResultLabel)
            emit("PUSH 0")
            emitLabel(endLabel)
            return
        }

        visitExpression(node.left)
        visitExpression(node.right)
        when (node.operator.type) {
            TokenType.PLUS -> emit("ADD")
            TokenType.MINUS -> emit("SUB")
            TokenType.MULTIPLY -> emit("MUL")
            TokenType.DIVIDE -> emit("DIV")
            TokenType.MODULO -> emit("MOD")
            TokenType.EQ -> emit("EQ")
            TokenType.NEQ -> emit("NE")
            TokenType.LT -> emit("LT")
            TokenType.GT -> emit("GT")
            TokenType.LTE -> emit("LTE")
            TokenType.GTE -> emit("GTE")
            TokenType.BITWISE_AND -> emit("B_AND")
            TokenType.BITWISE_OR -> emit("B_OR")
            TokenType.BITWISE_XOR -> emit("B_XOR")
            else -> throw CodeGenException("Unsupported binary operator: ${node.operator.type}")
        }
    }

    private fun visitUnaryOp(node: UnaryOpNode) {
        visitExpression(node.operand)
        when (node.operator.type) {
            TokenType.LOGICAL_NOT -> {
                emit("PUSH 0")
                emit("EQ")
            }
            TokenType.MINUS -> { 
                emit("PUSH 0") 
                emit("SWAP")   
                emit("SUB")    
            }
            TokenType.BITWISE_NOT -> emit("B_NOT")
            else -> throw CodeGenException("Unsupported unary operator: ${node.operator.type}")
        }
    }

    private fun visitFunctionCall(node: FunctionCallNode) {
        when (node.functionName) {
            "print" -> {
                if (node.arguments.size != 1) {
                    throw CodeGenException("'print' function expects 1 argument, got ${node.arguments.size}.")
                }
                visitExpression(node.arguments[0])
                emit("PRINT") 
            }
            "debug_print" -> {
                if (node.arguments.size != 1) {
                    throw CodeGenException("'debug_print' function expects 1 argument, got ${node.arguments.size}.")
                }
                visitExpression(node.arguments[0])
                emit("DEBUG_PRINT") 
            }
            else -> {
                val funcInfo = globalSymbolTable.lookup(node.functionName) as? SymbolTable.FunctionInfo
                    ?: throw CodeGenException("Function ${node.functionName} not found.")

                if (node.arguments.size != funcInfo.parameterCount) {
                    throw CodeGenException("Function ${node.functionName} called with incorrect number of arguments. Expected ${funcInfo.parameterCount}, got ${node.arguments.size}.")
                }

                node.arguments.forEach { visitExpression(it) }
                emit("CALL ${funcInfo.label}")
            }
        }
    }
}

class CodeGenException(message: String) : RuntimeException(message)
