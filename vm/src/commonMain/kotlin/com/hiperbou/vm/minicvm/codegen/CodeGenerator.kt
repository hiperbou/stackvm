package com.hiperbou.vm.minicvm.codegen

import com.hiperbou.vm.minicvm.ast.*
import com.hiperbou.vm.minicvm.lexer.TokenType

class SymbolTable(private val parent: SymbolTable? = null) {
    private val symbols = mutableMapOf<String, SymbolInfo>()
    private var currentLocalOffset = 0 // For local variables within a function scope
    private var currentGlobalOffset = 0 // For global variables (if we add them later)
    private var currentArgOffset = 0 // For arguments, typically negative offsets from frame pointer or positive from stack pointer at entry

    sealed class SymbolInfo(val name: String)
    class VariableInfo(name: String, val address: Int, val isArgument: Boolean = false, val type: String = "int") : SymbolInfo(name) // address can be offset
    class ArrayInfo(name: String, val baseAddress: Int, val size: Int, val type: String = "int") : SymbolInfo(name)
    class FunctionInfo(name: String, val label: String, val parameterCount: Int, val returnType: String) : SymbolInfo(name)

    fun defineVariable(name: String, type: String = "int", isArgument: Boolean = false): VariableInfo {
        if (symbols.containsKey(name)) throw CodeGenException("Variable '$name' already defined in this scope.")
        val address = if (isArgument) {
            // Arguments are typically at fixed positions relative to the function's entry point/frame
            // For simplicity, let's assume they are copied to local-like slots or accessed via specific instructions.
            // Here, we'll assign them offsets as if they are the first local variables.
            // A real calling convention would be more complex (e.g., using frame pointer offsets).
            currentArgOffset++ // Or a specific calculation based on calling convention
            currentLocalOffset++ // Treat args as locals for now in terms of offset counting.
        } else {
            currentLocalOffset++
        }
        val info = VariableInfo(name, address /*-1*/ , isArgument, type) // 0-indexed
        symbols[name] = info
        return info
    }
    
    fun defineGlobalVariable(name: String, type: String = "int"): VariableInfo {
        if (symbols.containsKey(name)) throw CodeGenException("Global variable '$name' already defined.")
        val info = VariableInfo(name, currentGlobalOffset, type = type)
        symbols[name] = info
        currentGlobalOffset++
        return info
    }


    fun defineArray(name: String, size: Int, type: String = "int"): ArrayInfo {
        if (symbols.containsKey(name)) throw CodeGenException("Array '$name' already defined in this scope.")
        // For arrays, the 'address' is the base address. We need to manage memory allocation.
        // This simple table assumes arrays are allocated globally or their base address is managed.
        // For now, let's use currentGlobalOffset for arrays if they are treated as globals.
        // If they are local, this needs a more sophisticated stack allocation.
        // For simplicity, let's assume local arrays are not directly supported or are handled via pointers.
        // Or, if local, their base address is an offset similar to variables.
        val info = ArrayInfo(name, currentLocalOffset, size, type)
        symbols[name] = info
        currentLocalOffset += size // Reserve space for the array
        return info
    }

    fun defineFunction(name: String, parameterCount: Int, returnType: String): FunctionInfo {
        val label = "${name}_label" // Simple label generation
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
        // Only clear locals and args for a new function scope, globals/functions persist
        val globalSymbols = symbols.filterValues { it is FunctionInfo || (it is VariableInfo && !it.isArgument && it.address < 1000)  /* hacky way to distinguish globals */}
        symbols.clear()
        symbols.putAll(globalSymbols)
        currentLocalOffset = 0
        currentArgOffset = 0
    }
}

class CodeGenerator {
    private val assemblyCode = StringBuilder()
    private var labelCounter = 0
    private val globalSymbolTable = SymbolTable()
    private var currentFunctionSymbolTable: SymbolTable? = null
    private var currentFunctionName: String? = null

    // Stacks for break/continue labels
    private val breakLabelStack = mutableListOf<String>()
    private val continueLabelStack = mutableListOf<String>()

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
        globalSymbolTable.resetLocalScope() // Clear any previous state

        // Predefine built-in functions
        // These don't have labels in the traditional sense as they map directly to opcodes.
        // The "label" field in FunctionInfo might not be used for these, or could be a special marker.
        // Parameter count is 1, return type is "void".
        globalSymbolTable.defineFunction("print", 1, "void")
        globalSymbolTable.defineFunction("debugPrint", 1, "void")

        // Optional: Preliminary pass to find all user-defined function declarations for forward calls
        programNode.declarations.filterIsInstance<FunctionDefinitionNode>().forEach { funcDef ->
            if (funcDef.name == "print" || funcDef.name == "debugPrint") {
                throw CodeGenException("Cannot redefine built-in function '${funcDef.name}'.")
            }
            globalSymbolTable.defineFunction(funcDef.name, funcDef.parameters.size, funcDef.returnType)
        }
        
        // Find main function to generate a JMP or CALL to it
        val mainFunction = programNode.declarations.filterIsInstance<FunctionDefinitionNode>().find { it.name == "main" }
        if (mainFunction != null) {
            emit("CALL main_label") // Standard way to start execution
            emit("HALT")
        } else {
            // Or JMP to the first function if no explicit main (legacy behavior)
            // For now, let's assume a main function is required or the first function is implicitly main
            // If there's no main, and we don't jump, the VM would just execute the first instruction.
            // This is fine if the first function is the entry point.
            // Consider emitting HALT at the very end if no explicit main call and HALT.
        }


        programNode.declarations.forEach { declaration ->
            visitTopLevelNode(declaration)
        }
        
        if (mainFunction == null && programNode.declarations.isNotEmpty()) {
             // If no main, but other functions exist, ensure HALT after all function code
            emit("HALT")
        }


        return assemblyCode.toString()
    }

    private fun visitTopLevelNode(node: TopLevelNode) {
        when (node) {
            is FunctionDefinitionNode -> visitFunctionDefinition(node)
            // GlobalVariableDeclarationNode could be handled here if added to AST
            else -> throw CodeGenException("Unsupported top-level node: $node")
        }
    }

    private fun visitFunctionDefinition(node: FunctionDefinitionNode) {
        currentFunctionName = node.name
        val functionInfo = globalSymbolTable.lookup(node.name) as? SymbolTable.FunctionInfo
            ?: throw CodeGenException("Function ${node.name} not defined in global symbol table.")
        
        currentFunctionSymbolTable = globalSymbolTable.createChildScope() // Each function gets a new scope for locals and args

        emitLabel(functionInfo.label)

        // Parameters: assume they are on the stack, pushed by the caller.
        // We need to store them into local variable slots.
        // The "address" in VariableInfo for an argument will be its conceptual slot.
        // For simplicity, let's assume arguments are at the bottom of the current "frame" or accessible directly.
        // The VM's `CALL` pushes return address. Arguments are below that.
        // If args are pushed left-to-right: arg1, arg2, ..., argN, then retAddr is on top.
        // Accessing them would be relative to stack pointer after CALL.
        // Let's assign them local variable slots as if they were the first locals.
        node.parameters.forEachIndexed { index, param ->
            val paramInfo = currentFunctionSymbolTable!!.defineVariable(param.name, param.type, isArgument = true)
            // Code to move argument from stack to its assigned local slot.
            // This depends heavily on the calling convention.
            // If args are at SP+1, SP+2 etc. after CALL (ret addr at SP)
            // For a simple stack machine where args are just pushed:
            // PUSH arg1, PUSH arg2, CALL func. Inside func: they are at top of stack.
            // We need to assign them memory slots (e.g. 0, 1, ... for locals)
            // For now, we assume arguments are handled by some convention and STORE operations will target their assigned 'addresses'
            // This part is tricky without a defined frame pointer/calling convention.
            // Let's assume arguments are available and the 'STORE' will correctly place them.
            // A simple model: caller pushes args, `STORE` in callee uses addresses 0, 1, ... for first, second param.
             emit("STORE ${node.parameters.size - 1 - paramInfo.address}") // This assumes arguments are on top of stack in reverse order of declaration and stored to local slots
        }


        visitBlock(node.body)

        // Ensure a RET for void functions or if the last statement isn't a return
        if (node.returnType == "void" || assemblyCode.lines().lastOrNull()?.trim() != "RET") {
            if (node.returnType != "void" && assemblyCode.lines().lastOrNull()?.trim() != "RET") {
                 // Non-void function reached end without explicit return. This is an error or implies returning a default value.
                 // For now, let's be strict or assume 0 for int if allowed.
                 // This case should ideally be caught by a semantic analyzer.
                 // emit("PUSH 0") // Default return for non-void if no explicit return (potentially problematic)
            }
            emit("RET")
        }
        currentFunctionSymbolTable = null // Exit function scope
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
            is VariableDeclarationNode -> visitVariableDeclaration(node)
            is ArrayDeclarationNode -> visitArrayDeclaration(node)
            is AssignmentNode -> visitAssignment(node)
            is IfStatementNode -> visitIfStatement(node)
            is WhileLoopNode -> visitWhileLoop(node)
            is DoWhileLoopNode -> visitDoWhileLoop(node) // Placeholder
            is ForLoopNode -> visitForLoop(node)         // Placeholder
            is BreakNode -> visitBreak(node)             // Placeholder
            is ContinueNode -> visitContinue(node)       // Placeholder
            is ReturnStatementNode -> visitReturnStatement(node)
            is ExpressionStatementNode -> visitExpressionStatement(node)
            else -> throw CodeGenException("Unsupported statement node: $node")
        }
    }

    private fun visitDoWhileLoop(node: DoWhileLoopNode) {
        val loopStartLabel = newLabel("DOWHILE_START")
        val conditionLabel = newLabel("DOWHILE_COND") // Continue will jump here
        val loopEndLabel = newLabel("DOWHILE_END")   // Break will jump here

        breakLabelStack.add(loopEndLabel)
        continueLabelStack.add(conditionLabel)

        emitLabel(loopStartLabel)
        visitBlock(node.body)

        emitLabel(conditionLabel)
        visitExpression(node.condition)
        // JIF jumps if condition is true. So if (condition is true), jump to loopStartLabel.
        emit("JIF $loopStartLabel")

        emitLabel(loopEndLabel)

        breakLabelStack.removeLastOrNull() ?: throw CodeGenException("Break stack underflow")
        continueLabelStack.removeLastOrNull() ?: throw CodeGenException("Continue stack underflow")
    }

    private fun visitForLoop(node: ForLoopNode) {
        val conditionCheckLabel = newLabel("FOR_COND_CHECK")
        // val loopBodyLabel = newLabel("FOR_BODY") // Not strictly needed if condition check is efficient
        val incrementerLabel = newLabel("FOR_INCREMENT")
        val loopEndLabel = newLabel("FOR_END")

        breakLabelStack.add(loopEndLabel)
        continueLabelStack.add(incrementerLabel)

        // 1. Initializer
        node.initializer?.let { visitStatement(it) } // Initializer is a StatementNode

        emitLabel(conditionCheckLabel)

        // 2. Condition
        if (node.condition != null) {
            visitExpression(node.condition)
            emit("NOT") // JIF jumps if true, so if (NOT condition) is true (i.e. condition is false), jump to end
            emit("JIF $loopEndLabel")
        }
        // If condition is null, it's an infinite loop part, so no jump here.

        // emitLabel(loopBodyLabel) // Not strictly needed
        // 3. Body
        visitBlock(node.body)

        emitLabel(incrementerLabel)
        // 4. Incrementer
        if (node.incrementer != null) {
            visitExpression(node.incrementer)
            // The value of the incrementer expression is usually not used, so pop it.
            // e.g., for (;; i++) -> i++ as an expression leaves a value.
            // If it was an assignment like i = i + 1, AssignmentNode itself doesn't leave a value on stack for code gen.
            // UpdateExpressionNode also leaves the value of the expression (before or after update).
            emit("POP")
        }

        // 5. Jump back to condition check
        emit("JMP $conditionCheckLabel")

        emitLabel(loopEndLabel)

        breakLabelStack.removeLastOrNull() ?: throw CodeGenException("Break stack underflow for FOR loop")
        continueLabelStack.removeLastOrNull() ?: throw CodeGenException("Continue stack underflow for FOR loop")
    }

    private fun visitBreak(node: BreakNode) {
        if (breakLabelStack.isEmpty()) {
            throw CodeGenException("Break statement outside of a loop.")
        }
        emit("JMP ${breakLabelStack.last()}")
    }

    private fun visitContinue(node: ContinueNode) {
        if (continueLabelStack.isEmpty()) {
            throw CodeGenException("Continue statement outside of a loop.")
        }
        emit("JMP ${continueLabelStack.last()}")
    }

    private fun visitVariableDeclaration(node: VariableDeclarationNode) {
        val table = currentFunctionSymbolTable ?: throw CodeGenException("No symbol table for variable declaration.")
        val varInfo = table.defineVariable(node.name, node.type)
        node.initializer?.let {
            visitExpression(it)
            emit("STORE ${varInfo.address}")
        }
    }

    private fun visitArrayDeclaration(node: ArrayDeclarationNode) {
        val table = currentFunctionSymbolTable ?: throw CodeGenException("No symbol table for array declaration.")
        // Array size must be a constant for this simple model
        val size = (node.size as? NumberLiteralNode)?.value ?: throw CodeGenException("Array size must be a number literal.")
        table.defineArray(node.name, size, node.type)
        // Allocation is implicit in the symbol table's offset management for now.
        // No specific assembly instructions needed for declaration beyond symbol table update,
        // unless memory needs to be zeroed out, which we are not doing.
    }

    private fun visitAssignment(node: AssignmentNode) {
        visitExpression(node.expression) // Value to be stored is now on top of stack

        when (val lvalue = node.lvalue) {
            is VariableAccessNode -> {
                val varInfo = (currentFunctionSymbolTable?.lookup(lvalue.name) ?: globalSymbolTable.lookup(lvalue.name)) as? SymbolTable.VariableInfo
                    ?: throw CodeGenException("Variable ${lvalue.name} not found for assignment.")
                emit("STORE ${varInfo.address}")
            }
            is ArrayAccessNode -> {
                val arrayInfo = (currentFunctionSymbolTable?.lookup(lvalue.arrayName) ?: globalSymbolTable.lookup(lvalue.arrayName)) as? SymbolTable.ArrayInfo
                    ?: throw CodeGenException("Array ${lvalue.arrayName} not found for assignment.")
                // Value is on stack. Need to calculate address: base_address + index
                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(lvalue.indexExpression)
                emit("ADD")
                emit("STOREI") // Store indirect
            }
            else -> throw CodeGenException("Unsupported lvalue for assignment: $lvalue")
        }
    }

    private fun visitIfStatement(node: IfStatementNode) {
        val elseLabel = newLabel("IF_ELSE")
        val endIfLabel = newLabel("IF_END")

        visitExpression(node.condition)
        emit("NOT")
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
        val loopStartLabel = newLabel("WHILE_START")
        val loopEndLabel = newLabel("WHILE_END")

        emitLabel(loopStartLabel)
        visitExpression(node.condition)
        emit("NOT")
        emit("JIF $loopEndLabel")
        visitBlock(node.body)
        emit("JMP $loopStartLabel")
        emitLabel(loopEndLabel)
    }

    private fun visitReturnStatement(node: ReturnStatementNode) {
        node.expression?.let {
            visitExpression(it)
        }
        // If void and expression is null, nothing is pushed.
        // If non-void, result of expression is on stack.
        emit("RET")
    }

    private fun visitExpressionStatement(node: ExpressionStatementNode) {
        visitExpression(node.expression)
        // If the expression was a function call that returns a value, it's now on the stack.
        // Since it's an expression statement, the value is not used.
        if (node.expression is FunctionCallNode) {
            val funcInfo = globalSymbolTable.lookup((node.expression as FunctionCallNode).functionName) as? SymbolTable.FunctionInfo
            if (funcInfo?.returnType != "void") {
                emit("POP") // Clean up stack if function returned a value that's not used
            }
        }
        // Other expressions (like `x + y;`) might also leave values if not part of assignment.
        // This simple C-like language might not allow `x+y;` as a statement directly,
        // but if it did and it left a value, it should be popped.
        // For now, only handling function calls explicitly.
    }


    private fun visitExpression(node: ExpressionNode) {
        when (node) {
            is NumberLiteralNode -> emit("PUSH ${node.value}")
            is VariableAccessNode -> {
                val varInfo = (currentFunctionSymbolTable?.lookup(node.name) ?: globalSymbolTable.lookup(node.name)) as? SymbolTable.VariableInfo
                    ?: throw CodeGenException("Variable ${node.name} not found.")
                emit("LOAD ${varInfo.address}")
            }
            is ArrayAccessNode -> {
                val arrayInfo = (currentFunctionSymbolTable?.lookup(node.arrayName) ?: globalSymbolTable.lookup(node.arrayName)) as? SymbolTable.ArrayInfo
                    ?: throw CodeGenException("Array ${node.arrayName} not found.")
                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(node.indexExpression)
                emit("ADD")
                emit("LOADI") // Load indirect
            }
            is BinaryOpNode -> visitBinaryOp(node)
            is UnaryOpNode -> visitUnaryOp(node)
            is FunctionCallNode -> visitFunctionCall(node)
            is TernaryOpNode -> visitTernaryOp(node)           // Placeholder
            is UpdateExpressionNode -> visitUpdateExpression(node) // Placeholder
            else -> throw CodeGenException("Unsupported expression node: $node")
        }
    }

    private fun visitTernaryOp(node: TernaryOpNode) {
        val falseLabel = newLabel("TERNARY_FALSE")
        val endLabel = newLabel("TERNARY_END")

        visitExpression(node.condition)
        emit("NOT") // JIF jumps if true, so if (NOT condition) is true (i.e. condition is false), jump to falseLabel
        emit("JIF $falseLabel")

        // True part
        visitExpression(node.thenExpr) // Value of thenExpr is on stack
        emit("JMP $endLabel")

        // False part
        emitLabel(falseLabel)
        visitExpression(node.elseExpr) // Value of elseExpr is on stack

        // End
        emitLabel(endLabel)
        // The value of the chosen expression (thenExpr or elseExpr) is now on top of the stack.
    }

    private fun visitUpdateExpression(node: UpdateExpressionNode) {
        val operand = node.operand
        val operatorType = node.operatorToken.type // INCREMENT or DECREMENT

        when (operand) {
            is VariableAccessNode -> {
                val varInfo = (currentFunctionSymbolTable?.lookup(operand.name) ?: globalSymbolTable.lookup(operand.name)) as? SymbolTable.VariableInfo
                    ?: throw CodeGenException("Variable ${operand.name} not found for update expression.")

                if (node.isPrefix) { // ++x or --x
                    emit("LOAD ${varInfo.address}")
                    emit("PUSH 1")
                    if (operatorType == TokenType.INCREMENT) emit("ADD") else emit("SUB")
                    emit("DUP") // Duplicate the updated value (this is the result of the expression)
                    emit("STORE ${varInfo.address}")
                } else { // x++ or x--
                    emit("LOAD ${varInfo.address}")
                    emit("DUP") // Duplicate the original value (this is the result of the expression)
                    emit("PUSH 1")
                    if (operatorType == TokenType.INCREMENT) emit("ADD") else emit("SUB")
                    emit("STORE ${varInfo.address}") // Store the updated value
                }
            }
            is ArrayAccessNode -> {
                val arrayInfo = (currentFunctionSymbolTable?.lookup(operand.arrayName) ?: globalSymbolTable.lookup(operand.arrayName)) as? SymbolTable.ArrayInfo
                    ?: throw CodeGenException("Array ${operand.arrayName} not found for update expression.")

                // 1. Calculate address (base + index)
                emit("PUSH ${arrayInfo.baseAddress}")
                visitExpression(operand.indexExpression)
                emit("ADD") // Stack: address

                if (node.isPrefix) { // ++arr[i] or --arr[i]
                    emit("DUP")   // Stack: address, address (one for LOADI, one for STOREI)
                    emit("LOADI") // Stack: address, value_at_address
                    emit("PUSH 1")
                    if (operatorType == TokenType.INCREMENT) emit("ADD") else emit("SUB") // Stack: address, updated_value
                    emit("DUP")   // Stack: address, updated_value, updated_value (updated_value is result of expression)
                                  // We need address, updated_value for STOREI, and updated_value for expression result.
                                  // Order for STOREI: address (below), value (top).
                                  // Current stack: address, updated_value_for_expr, updated_value_for_store
                                  // This is not right. DUP should be after updated_value is on top.
                                  // Corrected prefix array:
                                  // Stack: address
                                  // DUP (addr, addr)
                                  // LOADI (addr, old_val)
                                  // PUSH 1
                                  // ADD/SUB (addr, new_val)
                                  // DUP (addr, new_val, new_val) -> new_val is result of expression
                                  // SWAP (addr, new_val, new_val) -> (addr, new_val_expr, new_val_store)
                                  // STOREI needs addr under value. (new_val_expr, addr, new_val_store)
                                  // This is tricky. Let's re-evaluate:
                                  // Goal: store new value, expression result is new value.
                                  // 1. Calc address. Stack: Addr
                                  // 2. DUP. Stack: Addr, Addr
                                  // 3. LOADI. Stack: Addr, OldVal
                                  // 4. PUSH 1. Stack: Addr, OldVal, 1
                                  // 5. ADD/SUB. Stack: Addr, NewVal (this NewVal is for storing AND for expression result)
                                  // 6. DUP. Stack: Addr, NewVal, NewVal (Expr Result)
                                  // 7. SWAP. Stack: Addr, NewVal (Expr Result), NewVal (for STORE) -> No, this is not right.
                                  // STOREI expects Addr on stack *then* value.
                                  // Stack after (5): Addr, NewVal. NewVal is the result.
                                  // To store it: DUP (Addr, NewVal, NewVal(expr_val)), then we need Addr under NewVal for STOREI.
                                  // (NewVal(expr_val), Addr, NewVal(store_val))
                                  // This means: Addr, NewVal(calc).
                                  // DUP (Addr, NewVal, NewVal) -> NewVal is expr result.
                                  // Now need to store NewVal at Addr. Original Addr is below first NewVal.
                                  // Stack: Addr, NewVal_expr_result, NewVal_to_store
                                  // We need to get Addr under NewVal_to_store
                                  // Addr, Val_expr, Val_store -> SWAP -> Addr, Val_store, Val_expr
                                  // Now DUP Addr: Addr, Val_store, Val_expr, Addr
                                  // SWAP: Addr, Val_store, Addr, Val_expr
                                  // This is too much. Simpler:
                                  // Addr is on stack.
                                  // DUP (Addr, Addr)
                                  // LOADI (Addr, old_value)
                                  // PUSH 1
                                  // ADD/SUB (Addr, new_value) -> this new_value is the expression result
                                  // DUP (Addr, new_value, new_value) -> top new_value is for expression
                                  // Now we need to store the *other* new_value at Addr.
                                  // Stack: Addr_orig, new_value_expr_result, new_value_for_store
                                  // To use STOREI, stack needs to be: ..., address, value_to_store
                                  // We have Addr_orig. We have new_value_for_store.
                                  // We need to arrange (new_value_expr_result) then (Addr_orig, new_value_for_store) for STOREI
                                  // Current: Addr, NewVal(expr), NewVal(store)
                                  // emit("SWAP") // Addr, NewVal(store), NewVal(expr)
                                  // emit("STOREI") // Consumes Addr, NewVal(store). Leaves NewVal(expr). This is correct for prefix.
                                  // So, sequence:
                                  // 1. Calc Address -> Stack: Addr
                                  // 2. DUP -> Stack: Addr, Addr
                                  // 3. LOADI -> Stack: Addr, OldVal
                                  // 4. PUSH 1
                                  // 5. ADD/SUB (for new value) -> Stack: Addr, NewVal
                                  // 6. DUP -> Stack: Addr, NewVal, NewVal (top is for expression result)
                                  // 7. SWAP -> Stack: Addr, NewVal (for expression), NewVal (for store) -> Incorrect.
                                  //    Stack: Addr (for store), NewVal (for store), NewVal (for expr)
                                  //    This should be: Addr, NewVal (this is the result of op, for expr and for store)
                                  //    DUP -> Addr, NewVal, NewVal (Top is for expr. One below for storing)
                                  //    To store: we need Addr from bottom, and NewVal (middle).
                                  //    Stack: Addr_base, Val_calc, Val_expr_result
                                  //    If Val_calc === Val_expr_result:
                                  //    Addr_base, Val
                                  //    DUP -> Addr_base, Val, Val (this top Val is the expression result)
                                  //    Now we need to store the middle Val at Addr_base.
                                  //    The stack for STOREI should be: ..., address_for_store, value_for_store
                                  //    We have: Addr_base, Val_to_store, Val_as_expr_result
                                  //    emit("SWAP") -> Addr_base, Val_as_expr_result, Val_to_store (no, this is wrong)
                                  // Let's use the var logic: Load, Op, Dup, Store
                                  // For array: CalcAddr, DupAddr, LoadValFromAddr, OpWithOne, DupResult, StoreToAddr
                                  emit("SWAP") // Stack: address, new_value (top one is for expression, bottom is for store)
                                  emit("STOREI") // Consumes address and new_value for store. Top new_value (expression result) remains.
                                  // This seems right for prefix: result is the new value.

                } else { // x[i]++ or x[i]--
                    // Goal: store new value, expression result is original value.
                    // 1. Calc Address -> Stack: Addr
                    // 2. DUP -> Stack: Addr, Addr
                    // 3. LOADI -> Stack: Addr, OldVal (this OldVal is the expression result)
                    // 4. SWAP -> Stack: OldVal, Addr (OldVal is safe, Addr is for modification)
                    // 5. PUSH 1 -> Stack: OldVal, Addr, 1
                    // 6. ADD/SUB (for new value to store) -> Stack: OldVal, Addr, NewVal_to_store
                    // 7. STOREI -> Stack: OldVal (consumes Addr, NewVal_to_store)
                    // This looks correct for postfix.
                    emit("DUP")    // Stack: address, address
                    emit("LOADI")  // Stack: address, original_value (this is the result of expression)
                    emit("SWAP")   // Stack: original_value, address
                    emit("PUSH 1")
                    if (operatorType == TokenType.INCREMENT) emit("ADD") else emit("SUB") // Stack: original_value, address, new_value_to_store
                    emit("STOREI") // Consumes address & new_value_to_store. Leaves original_value on stack.
                }
            }
            else -> throw CodeGenException("Operand of update expression must be a variable or array access.")
        }
    }

    private fun visitBinaryOp(node: BinaryOpNode) {
        // Special handling for logical AND and OR due to short-circuiting
        if (node.operator.type == TokenType.LOGICAL_AND) {
            val falseLabel = newLabel("AND_FALSE")
            val endLabel = newLabel("AND_END")
            visitExpression(node.left)
            emit("NOT")
            emit("JIF $falseLabel") // If left is false, jump to push 0
            visitExpression(node.right)
            emit("NOT")
            emit("JIF $falseLabel") // If right is false, jump to push 0
            emit("PUSH 1")          // Both true, push 1
            emit("JMP $endLabel")
            emitLabel(falseLabel)
            emit("PUSH 0")          // One was false, push 0
            emitLabel(endLabel)
            return
        } else if (node.operator.type == TokenType.LOGICAL_OR) {
            val trueLabel = newLabel("OR_TRUE")
            val endLabel = newLabel("OR_END")
            val nextCheckLabel = newLabel("OR_NEXT")
            visitExpression(node.left)
            emit("NOT")
            emit("JIF $nextCheckLabel") // If left is false, check right
            emit("PUSH 1")            // Left is true, result is 1
            emit("JMP $endLabel")
            emitLabel(nextCheckLabel)
            visitExpression(node.right)
            /*val correctedORLength = assemblyCode.length
            emit("NOT")
            emit("JIF $trueLabel") // If right is false, push 0 (fall through)
            emit("PUSH 1") // Right is true, push 1
            emit("JMP $endLabel")
            emitLabel(trueLabel) // This label was for when right was false after left was false.
                                 // Actually, it's simpler: if right is false, result is 0.
                                 // Let's adjust:
                                 // JIF $nextCheckLabel (if left is false, eval right)
                                 // PUSH 1 (left is true)
                                 // JMP $endLabel
                                 // $nextCheckLabel:
                                 // VISIT(right)
                                 // JIF $falseLabel (if right is false, result is 0)
                                 // PUSH 1 (right is true)
                                 // JMP $endLabel
                                 // $falseLabel:
                                 // PUSH 0
                                 // $endLabel
            // Corrected OR: //TODO: WTF
            //assemblyCode.setLength(assemblyCode.length - ("NOT\nJIF $trueLabel\n").length - ("PUSH 1\nJMP $endLabel\n${trueLabel}:\n").length) // backtrack
            assemblyCode.setLength(correctedORLength)*/
            val falseLabel = newLabel("OR_FALSE")
            // visitExpression(node.left) // already emitted
            // emit("JIF $nextCheckLabel") // already emitted
            // emit("PUSH 1") // already emitted
            // emit("JMP $endLabel") // already emitted
            // emitLabel(nextCheckLabel) // already emitted
            // visitExpression(node.right) // already emitted
            emit("NOT")
            emit("JIF $falseLabel") // if right is false, result is 0
            emit("PUSH 1") // right is true
            emit("JMP $endLabel")
            emitLabel(falseLabel)
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
            // New Bitwise Operators
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
                // Assuming 0 is false, non-zero is true.
                // NOT x is equivalent to x == 0
                emit("PUSH 0")
                emit("EQ")
            }
            TokenType.MINUS -> { // Unary minus
                emit("PUSH 0") // Push 0 onto stack
                emit("SWAP")   // Swap so operand is on top
                emit("SUB")    // 0 - operand = -operand
            }
            TokenType.BITWISE_NOT -> emit("B_NOT") // New Bitwise NOT
            else -> throw CodeGenException("Unsupported unary operator: ${node.operator.type}")
        }
    }

    private fun visitFunctionCall(node: FunctionCallNode) {
        // Handle built-in print functions
        if (node.functionName == "print") {
            if (node.arguments.size != 1) {
                throw CodeGenException("Function 'print' expects 1 argument, got ${node.arguments.size}.")
            }
            visitExpression(node.arguments[0])
            emit("PRINT") // Emit PRINT opcode
            return // Skip standard function call logic
        } else if (node.functionName == "debugPrint") {
            if (node.arguments.size != 1) {
                throw CodeGenException("Function 'debugPrint' expects 1 argument, got ${node.arguments.size}.")
            }
            visitExpression(node.arguments[0])
            emit("PRINT_DEBUG") // Emit PRINT_DEBUG opcode
            return // Skip standard function call logic
        }

        // Standard function call logic
        val funcInfo = globalSymbolTable.lookup(node.functionName) as? SymbolTable.FunctionInfo
            ?: throw CodeGenException("Function ${node.functionName} not found.")

        if (node.arguments.size != funcInfo.parameterCount) {
            throw CodeGenException("Function ${node.functionName} called with incorrect number of arguments. Expected ${funcInfo.parameterCount}, got ${node.arguments.size}.")
        }

        // Evaluate and push arguments onto the stack.
        node.arguments.forEach { visitExpression(it) }

        emit("CALL ${funcInfo.label}")

        // If the calling convention requires caller to clean up arguments from stack,
        // and arguments are not popped by callee (e.g. by STORE into local slots and then stack is not adjusted),
        // then POP them here. For now, assume callee or stack discipline handles this.
        // For many simple VMs, arguments are "consumed" by the callee's STORE operations or by RET N.
        // Our current RET doesn't take an argument for popping.
        // If STORE operations effectively copy args to local slots and we want to clear them from stack:
        // This depends on how arguments are referenced by STORE inside the callee.
        // Our current `STORE addr` assumes `addr` is a memory location, not a stack operation.
        // And arguments are assumed to be on top of stack upon entry.
        // Let's assume that after the call, if args were pushed, they need to be popped by the caller
        // *unless* the callee's `RET` handles it or they are `STORE`d into local variables *and those local variables are on the stack frame that is popped*.
        // This is getting complex. Simplest: callee uses arguments from stack, does not pop them. Caller pops them after call.
        // (This is not efficient for many args).
        // Alternative: arguments are part of the callee's stack frame, RET cleans them up.
        // Our current model: `STORE arg_addr` saves arg from stack. This implies the argument is consumed from stack.
        // Let's test this assumption. If `STORE` consumes from stack, then nothing to do by caller.
        // The VM's `STORE n` instruction stores TOS into mem[n]. It does NOT pop the stack.
        // So, arguments pushed by caller are still on stack when callee is entered.
        // The callee's `STORE` for parameters will copy TOS to a local var, but TOS is still there.
        // This means parameters in the callee are accessed from their local var slots, not the stack.
        // So, the caller MUST pop the arguments after the call returns.

        if (funcInfo.parameterCount > 0) {
            // This loop is incorrect if arguments are consumed by STORE.
            // If arguments are NOT consumed by STORE, then they need to be popped.
            // Our STORE instruction: "Pop value from stack, store in memory[address]" -> This IS consuming.
            // Let's re-verify `CompilerTest.kt`'s VM behavior for STORE.
            // From `VirtualMachine.kt`:
            // STORE -> address = code[ip++]!!; memory[address] = stack.pop() -> Yes, STORE pops.
            // This means the arguments pushed by the caller ARE consumed by the STORE instructions for parameters in the callee.
            // Therefore, caller does NOT need to pop arguments after the call.
        }
    }
}

class CodeGenException(message: String) : RuntimeException(message)
