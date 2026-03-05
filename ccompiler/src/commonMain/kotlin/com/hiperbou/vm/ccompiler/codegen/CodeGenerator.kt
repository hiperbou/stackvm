package com.hiperbou.vm.ccompiler.codegen

import com.hiperbou.vm.InstructionsEnum
import com.hiperbou.vm.ccompiler.ast.AstNode
import com.hiperbou.vm.ccompiler.ast.BinaryOperator
import com.hiperbou.vm.ccompiler.ast.IncDecOperator
import com.hiperbou.vm.ccompiler.ast.UnaryOperator
import com.hiperbou.vm.compiler.DefaultProgramWriter
import com.hiperbou.vm.compiler.LabelResolver
import com.hiperbou.vm.compiler.ProgramWriter
import com.hiperbou.vm.plugin.print.PrintInstructionsEnum

/**
 * Walks the AST and emits VM bytecode via [ProgramWriter].
 *
 * Uses [LabelResolver] for forward references (function calls, jumps).
 *
 * **`print(expr)` behaviour**: emits `PRINT` + `POP` because the VM's PRINT uses peek()
 * and does not consume the value from the stack.
 *
 * **`main` entry point**: if `main` is not the first function, a `JMP main` is emitted
 * at address 0 so execution starts at `main`.
 *
 * **`return` in `main`**: emits `HALT` (the return value is discarded for now).
 * **`return` in other functions**: emits `RET`.
 */
class CodeGenerator(
    private val writer: ProgramWriter = DefaultProgramWriter(),
    private val symbols: SymbolTable = SymbolTable(),
    private val labelResolver: LabelResolver = LabelResolver()
) {

    /** Name of the function currently being compiled. */
    private var currentFunction: String = ""

    fun generate(program: AstNode.Program): IntArray {
        // If main is not the first function, emit a JMP to main at address 0.
        val mainFirst = program.functions.firstOrNull()?.name == "main"
        if (!mainFirst && program.functions.any { it.name == "main" }) {
            emitJmpToMain()
        }

        // First pass: register all function addresses (forward declarations).
        // We do a two-pass approach: emit all functions, then resolve labels.
        for (function in program.functions) {
            generateFunction(function)
        }

        labelResolver.resolveLabels(writer.program)
        return writer.program.toIntArray()
    }

    // -------------------------------------------------------------------------
    // JMP to main (emitted at address 0 when main is not first)
    // -------------------------------------------------------------------------

    private fun emitJmpToMain() {
        writer.addInstruction(InstructionsEnum.JMP)
        val patchAddress = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)
        labelResolver.addUnresolvedLabel("main", patchAddress) { "JMP main at address 0" }
    }

    // -------------------------------------------------------------------------
    // Function
    // -------------------------------------------------------------------------

    private fun generateFunction(function: AstNode.FunctionDecl) {
        val address = writer.currentAddress()
        symbols.registerFunction(function.name, address)
        labelResolver.addLabel(function.name, address) { "function ${function.name}" }

        currentFunction = function.name
        symbols.enterFunction()

        // Parameters are pushed by the caller before CALL (left-to-right),
        // so the last parameter is on top of the stack.
        // We declare all slots first, then store in reverse order (last param first).
        // NOTE: main parameters (argc, argv) are ignored for now.
        // MISSING FEATURE: main parameters are not loaded from memory.
        // See plans/ccompiler-plan.md for details.
        if (function.name != "main") {
            // First pass: declare all parameter slots
            for (param in function.params) {
                symbols.declareLocal(param.name)
            }
            // Second pass: store in reverse order (last param is on top of stack)
            for (param in function.params.asReversed()) {
                val slot = symbols.resolveLocal(param.name)!!
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(slot)
            }
        }

        generateBlock(function.body)

        symbols.exitFunction()
    }

    // -------------------------------------------------------------------------
    // Block
    // -------------------------------------------------------------------------

    private fun generateBlock(block: AstNode.Block) {
        symbols.enterBlock()
        for (stmt in block.statements) {
            generateStatement(stmt)
        }
        symbols.exitBlock()
    }

    // -------------------------------------------------------------------------
    // Statements
    // -------------------------------------------------------------------------

    private fun generateStatement(stmt: AstNode.Statement) {
        when (stmt) {
            is AstNode.VarDecl          -> generateVarDecl(stmt)
            is AstNode.AssignStatement  -> generateAssign(stmt)
            is AstNode.CompoundAssign   -> generateCompoundAssign(stmt)
            is AstNode.PrintStatement   -> generatePrint(stmt)
            is AstNode.ReturnStatement  -> generateReturn(stmt)
            is AstNode.IfStatement      -> generateIf(stmt)
            is AstNode.WhileStatement   -> generateWhile(stmt)
            is AstNode.ForStatement     -> generateFor(stmt)
            is AstNode.DoStatement      -> generateBlock(stmt.body)
            is AstNode.ExpressionStatement -> {
                generateExpression(stmt.expr)
                // Expression result left on stack — pop it (statement context discards value)
                writer.addInstruction(InstructionsEnum.POP)
            }
        }
    }

    /** `int x = expr;` or `int x;` (default 0) */
    private fun generateVarDecl(stmt: AstNode.VarDecl) {
        val slot = symbols.declareLocal(stmt.name)
        if (stmt.initializer != null) {
            generateExpression(stmt.initializer)
        } else {
            writer.addInstruction(InstructionsEnum.PUSH)
            writer.addLiteral(0)
        }
        writer.addInstruction(InstructionsEnum.STORE)
        writer.addLiteral(slot)
    }

    /** `x = expr;` */
    private fun generateAssign(stmt: AstNode.AssignStatement) {
        generateExpression(stmt.value)
        val slot = symbols.resolveLocal(stmt.name)
            ?: throw CodeGenException("Undefined variable '${stmt.name}'")
        writer.addInstruction(InstructionsEnum.STORE)
        writer.addLiteral(slot)
    }

    /** `x += expr;` etc. */
    private fun generateCompoundAssign(stmt: AstNode.CompoundAssign) {
        val slot = symbols.resolveLocal(stmt.name)
            ?: throw CodeGenException("Undefined variable '${stmt.name}'")
        writer.addInstruction(InstructionsEnum.LOAD)
        writer.addLiteral(slot)
        generateExpression(stmt.value)
        when (stmt.op) {
            BinaryOperator.ADD -> writer.addInstruction(InstructionsEnum.ADD)
            BinaryOperator.SUB -> writer.addInstruction(InstructionsEnum.SUB)
            BinaryOperator.MUL -> writer.addInstruction(InstructionsEnum.MUL)
            BinaryOperator.DIV -> writer.addInstruction(InstructionsEnum.DIV)
            BinaryOperator.MOD -> writer.addInstruction(InstructionsEnum.MOD)
            else -> throw CodeGenException("Unsupported compound assignment operator ${stmt.op}")
        }
        writer.addInstruction(InstructionsEnum.STORE)
        writer.addLiteral(slot)
    }

    /**
     * `print(expr);`
     * Emits: evaluate expr → PRINT → POP
     * (VM's PRINT uses peek(), so POP must be explicit)
     */
    private fun generatePrint(stmt: AstNode.PrintStatement) {
        generateExpression(stmt.expr)
        writer.addInstruction(PrintInstructionsEnum.PRINT)
        writer.addInstruction(InstructionsEnum.POP)
    }

    /**
     * `return expr;`
     * In `main`: emits HALT (return value discarded).
     * In other functions: emits RET (return value left on stack for caller).
     */
    private fun generateReturn(stmt: AstNode.ReturnStatement) {
        generateExpression(stmt.expr)
        if (currentFunction == "main") {
            // Discard the return value and halt
            writer.addInstruction(InstructionsEnum.POP)
            writer.addInstruction(InstructionsEnum.HALT)
        } else {
            writer.addInstruction(InstructionsEnum.RET)
        }
    }

    /**
     * `if (cond) thenBlock [else elseBlock]`
     *
     * Bytecode layout:
     * ```
     *   <cond>
     *   JIF  then_label
     *   JMP  else_label   (or end_label if no else)
     * then_label:
     *   <thenBlock>
     *   JMP  end_label    (only if there is an else)
     * else_label:
     *   <elseBlock>
     * end_label:
     * ```
     *
     * Note: JIF jumps if the top of stack is != 0 (truthy).
     */
    private fun generateIf(stmt: AstNode.IfStatement) {
        generateExpression(stmt.condition)

        // JIF → jump to then_label if condition is true
        writer.addInstruction(InstructionsEnum.JIF)
        val jifPatch = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

        if (stmt.elseBlock != null) {
            // JMP → skip then block, go to else
            writer.addInstruction(InstructionsEnum.JMP)
            val jmpElsePatch = writer.currentAddress()
            writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

            // then_label
            val thenAddress = writer.currentAddress()
            writer.program[jifPatch] = thenAddress
            generateBlock(stmt.thenBlock)

            // JMP → skip else block
            writer.addInstruction(InstructionsEnum.JMP)
            val jmpEndPatch = writer.currentAddress()
            writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

            // else_label
            val elseAddress = writer.currentAddress()
            writer.program[jmpElsePatch] = elseAddress
            generateBlock(stmt.elseBlock)

            // end_label
            val endAddress = writer.currentAddress()
            writer.program[jmpEndPatch] = endAddress
        } else {
            // JMP → skip then block (no else)
            writer.addInstruction(InstructionsEnum.JMP)
            val jmpEndPatch = writer.currentAddress()
            writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

            // then_label
            val thenAddress = writer.currentAddress()
            writer.program[jifPatch] = thenAddress
            generateBlock(stmt.thenBlock)

            // end_label
            val endAddress = writer.currentAddress()
            writer.program[jmpEndPatch] = endAddress
        }
    }

    /**
     * `while (cond) body`
     *
     * Bytecode layout:
     * ```
     * loop_label:
     *   <cond>
     *   JIF  body_label
     *   JMP  end_label
     * body_label:
     *   <body>
     *   JMP  loop_label
     * end_label:
     * ```
     */
    private fun generateWhile(stmt: AstNode.WhileStatement) {
        val loopAddress = writer.currentAddress()

        generateExpression(stmt.condition)

        writer.addInstruction(InstructionsEnum.JIF)
        val jifPatch = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

        writer.addInstruction(InstructionsEnum.JMP)
        val jmpEndPatch = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

        val bodyAddress = writer.currentAddress()
        writer.program[jifPatch] = bodyAddress
        generateBlock(stmt.body)

        writer.addInstruction(InstructionsEnum.JMP)
        writer.addLiteral(loopAddress)

        val endAddress = writer.currentAddress()
        writer.program[jmpEndPatch] = endAddress
    }

    /**
     * `for (init?; cond?; update?) body`
     *
     * Desugars to:
     * ```
     *   init
     * loop_label:
     *   cond (if present, else push 1)
     *   JIF  body_label
     *   JMP  end_label
     * body_label:
     *   body
     *   update
     *   JMP  loop_label
     * end_label:
     * ```
     */
    private fun generateFor(stmt: AstNode.ForStatement) {
        symbols.enterBlock()

        // init
        stmt.init?.let { generateStatement(it) }

        val loopAddress = writer.currentAddress()

        // condition (if absent, loop forever — treat as `while(1)`)
        if (stmt.condition != null) {
            generateExpression(stmt.condition)
        } else {
            writer.addInstruction(InstructionsEnum.PUSH)
            writer.addLiteral(1)
        }

        writer.addInstruction(InstructionsEnum.JIF)
        val jifPatch = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

        writer.addInstruction(InstructionsEnum.JMP)
        val jmpEndPatch = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

        val bodyAddress = writer.currentAddress()
        writer.program[jifPatch] = bodyAddress
        generateBlock(stmt.body)

        // update
        stmt.update?.let { generateStatement(it) }

        writer.addInstruction(InstructionsEnum.JMP)
        writer.addLiteral(loopAddress)

        val endAddress = writer.currentAddress()
        writer.program[jmpEndPatch] = endAddress

        symbols.exitBlock()
    }

    // -------------------------------------------------------------------------
    // Expressions
    // -------------------------------------------------------------------------

    private fun generateExpression(expr: AstNode.Expression) {
        when (expr) {
            is AstNode.NumberLiteral -> {
                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(expr.value)
            }
            is AstNode.Identifier -> {
                val slot = symbols.resolveLocal(expr.name)
                    ?: throw CodeGenException("Undefined variable '${expr.name}'")
                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(slot)
            }
            is AstNode.BinaryOp -> generateBinaryOp(expr)
            is AstNode.UnaryOp  -> generateUnaryOp(expr)
            is AstNode.FunctionCall -> generateFunctionCall(expr)
            is AstNode.PreIncDec  -> generatePreIncDec(expr)
            is AstNode.PostIncDec -> generatePostIncDec(expr)
            is AstNode.AssignExpr -> {
                // Assignment as expression: evaluate value, DUP (result stays on stack), STORE
                val slot = symbols.resolveLocal(expr.name)
                    ?: throw CodeGenException("Undefined variable '${expr.name}'")
                generateExpression(expr.value)
                writer.addInstruction(InstructionsEnum.DUP)
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(slot)
            }
            is AstNode.CompoundAssignExpr -> {
                // Compound assignment as expression: load, compute, DUP, STORE
                val slot = symbols.resolveLocal(expr.name)
                    ?: throw CodeGenException("Undefined variable '${expr.name}'")
                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(slot)
                generateExpression(expr.value)
                when (expr.op) {
                    BinaryOperator.ADD -> writer.addInstruction(InstructionsEnum.ADD)
                    BinaryOperator.SUB -> writer.addInstruction(InstructionsEnum.SUB)
                    BinaryOperator.MUL -> writer.addInstruction(InstructionsEnum.MUL)
                    BinaryOperator.DIV -> writer.addInstruction(InstructionsEnum.DIV)
                    BinaryOperator.MOD -> writer.addInstruction(InstructionsEnum.MOD)
                    else -> throw CodeGenException("Unsupported compound assignment operator ${expr.op}")
                }
                writer.addInstruction(InstructionsEnum.DUP)
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(slot)
            }
        }
    }

    private fun generateBinaryOp(expr: AstNode.BinaryOp) {
        generateExpression(expr.left)
        generateExpression(expr.right)
        when (expr.op) {
            BinaryOperator.ADD -> writer.addInstruction(InstructionsEnum.ADD)
            BinaryOperator.SUB -> writer.addInstruction(InstructionsEnum.SUB)
            BinaryOperator.MUL -> writer.addInstruction(InstructionsEnum.MUL)
            BinaryOperator.DIV -> writer.addInstruction(InstructionsEnum.DIV)
            BinaryOperator.MOD -> writer.addInstruction(InstructionsEnum.MOD)
            BinaryOperator.EQ  -> writer.addInstruction(InstructionsEnum.EQ)
            BinaryOperator.NE  -> writer.addInstruction(InstructionsEnum.NE)
            BinaryOperator.LT  -> writer.addInstruction(InstructionsEnum.LT)
            BinaryOperator.GT  -> writer.addInstruction(InstructionsEnum.GT)
            BinaryOperator.LTE -> writer.addInstruction(InstructionsEnum.LTE)
            BinaryOperator.GTE -> writer.addInstruction(InstructionsEnum.GTE)
            BinaryOperator.AND -> writer.addInstruction(InstructionsEnum.AND)
            BinaryOperator.OR  -> writer.addInstruction(InstructionsEnum.OR)
        }
    }

    private fun generateUnaryOp(expr: AstNode.UnaryOp) {
        generateExpression(expr.expr)
        when (expr.op) {
            UnaryOperator.NEG -> writer.addInstruction(InstructionsEnum.NEG)
            UnaryOperator.NOT -> writer.addInstruction(InstructionsEnum.NOT)
        }
    }

    private fun generateFunctionCall(expr: AstNode.FunctionCall) {
        // Push arguments left-to-right
        for (arg in expr.args) {
            generateExpression(arg)
        }
        writer.addInstruction(InstructionsEnum.CALL)
        val patchAddress = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)
        labelResolver.addUnresolvedLabel(expr.name, patchAddress) { "call to ${expr.name}" }
    }

    /** `++x` — increment then load new value */
    private fun generatePreIncDec(expr: AstNode.PreIncDec) {
        val slot = symbols.resolveLocal(expr.name)
            ?: throw CodeGenException("Undefined variable '${expr.name}'")
        writer.addInstruction(InstructionsEnum.LOAD)
        writer.addLiteral(slot)
        writer.addInstruction(InstructionsEnum.PUSH)
        writer.addLiteral(1)
        when (expr.op) {
            IncDecOperator.INC -> writer.addInstruction(InstructionsEnum.ADD)
            IncDecOperator.DEC -> writer.addInstruction(InstructionsEnum.SUB)
        }
        writer.addInstruction(InstructionsEnum.DUP)
        writer.addInstruction(InstructionsEnum.STORE)
        writer.addLiteral(slot)
    }

    /** `x++` — load old value, then increment (result of expression is old value) */
    private fun generatePostIncDec(expr: AstNode.PostIncDec) {
        val slot = symbols.resolveLocal(expr.name)
            ?: throw CodeGenException("Undefined variable '${expr.name}'")
        // Load current value (this is the expression result)
        writer.addInstruction(InstructionsEnum.LOAD)
        writer.addLiteral(slot)
        // Compute new value
        writer.addInstruction(InstructionsEnum.LOAD)
        writer.addLiteral(slot)
        writer.addInstruction(InstructionsEnum.PUSH)
        writer.addLiteral(1)
        when (expr.op) {
            IncDecOperator.INC -> writer.addInstruction(InstructionsEnum.ADD)
            IncDecOperator.DEC -> writer.addInstruction(InstructionsEnum.SUB)
        }
        // Store new value (old value remains on stack as expression result)
        writer.addInstruction(InstructionsEnum.STORE)
        writer.addLiteral(slot)
    }
}

