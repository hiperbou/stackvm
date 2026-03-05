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

class CodeGenerator(
    private val writer: ProgramWriter = DefaultProgramWriter(),
    private val symbols: SymbolTable = SymbolTable(),
    private val labelResolver: LabelResolver = LabelResolver()
) {

    private data class LoopContext(
        val breakPatches: MutableList<Int> = mutableListOf(),
        val continuePatches: MutableList<Int> = mutableListOf()
    )

    private data class AddressRef(val baseSlot: Int, val isGlobal: Boolean)

    private var currentFunction: String = ""
    private var tempCounter: Int = 0
    private val loopContexts = mutableListOf<LoopContext>()

    fun generate(program: AstNode.Program): IntArray {
        generateGlobalInitializers(program.globals)

        val mainFirst = program.functions.firstOrNull()?.name == "main"
        if (!mainFirst && program.functions.any { it.name == "main" }) {
            emitJmpToMain()
        }

        for (function in program.functions) {
            generateFunction(function)
        }

        labelResolver.resolveLabels(writer.program)
        return writer.program.toIntArray()
    }

    private fun generateGlobalInitializers(globals: List<AstNode.GlobalVarDecl>) {
        for (global in globals) {
            val slot = symbols.declareGlobal(global.name)
            if (global.initializer != null) {
                generateExpression(global.initializer)
            } else {
                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(0)
            }
            writer.addInstruction(InstructionsEnum.GSTORE)
            writer.addLiteral(slot)
        }
    }

    private fun emitJmpToMain() {
        writer.addInstruction(InstructionsEnum.JMP)
        val patchAddress = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)
        labelResolver.addUnresolvedLabel("main", patchAddress) { "JMP main" }
    }

    private fun generateFunction(function: AstNode.FunctionDecl) {
        val address = writer.currentAddress()
        symbols.registerFunction(function.name, address)
        labelResolver.addLabel(function.name, address) { "function ${function.name}" }

        currentFunction = function.name
        symbols.enterFunction()
        tempCounter = 0

        if (function.name != "main") {
            for (param in function.params) {
                symbols.declareLocal(param.name)
            }
            for (param in function.params.asReversed()) {
                val slot = symbols.resolveLocal(param.name)!!
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(slot)
            }
        }

        generateBlock(function.body)
        symbols.exitFunction()
    }

    private fun generateBlock(block: AstNode.Block) {
        symbols.enterBlock()
        for (stmt in block.statements) {
            generateStatement(stmt)
        }
        symbols.exitBlock()
    }

    private fun generateStatement(stmt: AstNode.Statement) {
        when (stmt) {
            is AstNode.VarDecl -> generateVarDecl(stmt)
            is AstNode.VarDeclList -> stmt.declarations.forEach { generateVarDecl(it) }
            is AstNode.ArrayDecl -> generateArrayDecl(stmt)
            is AstNode.AssignStatement -> generateAssign(stmt)
            is AstNode.ArrayAssignStatement -> generateArrayAssign(stmt.name, stmt.index, stmt.value)
            is AstNode.CompoundAssign -> generateCompoundAssign(stmt)
            is AstNode.PrintStatement -> generatePrint(stmt)
            is AstNode.DebugPrintStatement -> generateDebugPrint(stmt)
            is AstNode.ReturnStatement -> generateReturn(stmt)
            is AstNode.IfStatement -> generateIf(stmt)
            is AstNode.WhileStatement -> generateWhile(stmt)
            is AstNode.ForStatement -> generateFor(stmt)
            is AstNode.DoStatement -> generateBlock(stmt.body)
            is AstNode.DoWhileStatement -> generateDoWhile(stmt)
            is AstNode.BreakStatement -> generateBreak()
            is AstNode.ContinueStatement -> generateContinue()
            is AstNode.ExpressionStatement -> {
                generateExpression(stmt.expr)
                writer.addInstruction(InstructionsEnum.POP)
            }
        }
    }

    private fun beginLoopContext() {
        loopContexts.add(LoopContext())
    }

    private fun currentLoopContext(): LoopContext =
        loopContexts.lastOrNull() ?: throw CodeGenException("break/continue used outside of a loop")

    private fun endLoopContext(breakTarget: Int, continueTarget: Int) {
        val ctx = loopContexts.removeAt(loopContexts.lastIndex)
        for (patch in ctx.breakPatches) writer.program[patch] = breakTarget
        for (patch in ctx.continuePatches) writer.program[patch] = continueTarget
    }

    private fun generateBreak() {
        val ctx = currentLoopContext()
        writer.addInstruction(InstructionsEnum.JMP)
        val patchAddress = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)
        ctx.breakPatches.add(patchAddress)
    }

    private fun generateContinue() {
        val ctx = currentLoopContext()
        writer.addInstruction(InstructionsEnum.JMP)
        val patchAddress = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)
        ctx.continuePatches.add(patchAddress)
    }

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

    private fun generateArrayDecl(stmt: AstNode.ArrayDecl) {
        val base = symbols.declareLocal(stmt.name, stmt.size)
        for (offset in 0 until stmt.size) {
            writer.addInstruction(InstructionsEnum.PUSH)
            writer.addLiteral(0)
            writer.addInstruction(InstructionsEnum.STORE)
            writer.addLiteral(base + offset)
        }
    }

    private fun generateAssign(stmt: AstNode.AssignStatement) {
        generateExpression(stmt.value)
        writeStore(stmt.name)
    }

    private fun generateArrayAssign(name: String, index: AstNode.Expression, value: AstNode.Expression) {
        generateExpression(value)
        emitIndexedAddress(name, index)
        if (resolveAddress(name).isGlobal) {
            writer.addInstruction(InstructionsEnum.GSTOREI)
        } else {
            writer.addInstruction(InstructionsEnum.STOREI)
        }
    }

    private fun generateCompoundAssign(stmt: AstNode.CompoundAssign) {
        writeLoad(stmt.name)
        generateExpression(stmt.value)
        emitBinaryOp(stmt.op)
        writeStore(stmt.name)
    }

    private fun generatePrint(stmt: AstNode.PrintStatement) {
        generateExpression(stmt.expr)
        writer.addInstruction(PrintInstructionsEnum.PRINT)
        writer.addInstruction(InstructionsEnum.POP)
    }

    private fun generateDebugPrint(stmt: AstNode.DebugPrintStatement) {
        generateExpression(stmt.expr)
        writer.addInstruction(PrintInstructionsEnum.DEBUG_PRINT)
        writer.addInstruction(InstructionsEnum.POP)
    }

    private fun generateReturn(stmt: AstNode.ReturnStatement) {
        generateExpression(stmt.expr)
        if (currentFunction == "main") {
            writer.addInstruction(InstructionsEnum.POP)
            writer.addInstruction(InstructionsEnum.HALT)
        } else {
            writer.addInstruction(InstructionsEnum.RET)
        }
    }

    private fun generateIf(stmt: AstNode.IfStatement) {
        generateExpression(stmt.condition)

        writer.addInstruction(InstructionsEnum.JIF)
        val jifPatch = writer.currentAddress()
        writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

        if (stmt.elseBlock != null) {
            writer.addInstruction(InstructionsEnum.JMP)
            val jmpElsePatch = writer.currentAddress()
            writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

            val thenAddress = writer.currentAddress()
            writer.program[jifPatch] = thenAddress
            generateBlock(stmt.thenBlock)

            writer.addInstruction(InstructionsEnum.JMP)
            val jmpEndPatch = writer.currentAddress()
            writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

            val elseAddress = writer.currentAddress()
            writer.program[jmpElsePatch] = elseAddress
            generateBlock(stmt.elseBlock)

            val endAddress = writer.currentAddress()
            writer.program[jmpEndPatch] = endAddress
        } else {
            writer.addInstruction(InstructionsEnum.JMP)
            val jmpEndPatch = writer.currentAddress()
            writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

            val thenAddress = writer.currentAddress()
            writer.program[jifPatch] = thenAddress
            generateBlock(stmt.thenBlock)

            val endAddress = writer.currentAddress()
            writer.program[jmpEndPatch] = endAddress
        }
    }

    private fun generateWhile(stmt: AstNode.WhileStatement) {
        beginLoopContext()

        val condAddress = writer.currentAddress()
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
        writer.addLiteral(condAddress)

        val endAddress = writer.currentAddress()
        writer.program[jmpEndPatch] = endAddress

        endLoopContext(endAddress, condAddress)
    }

    private fun generateDoWhile(stmt: AstNode.DoWhileStatement) {
        beginLoopContext()

        val bodyAddress = writer.currentAddress()
        generateBlock(stmt.body)

        val condAddress = writer.currentAddress()
        generateExpression(stmt.condition)
        writer.addInstruction(InstructionsEnum.JIF)
        writer.addLiteral(bodyAddress)

        val endAddress = writer.currentAddress()
        endLoopContext(endAddress, condAddress)
    }

    private fun generateFor(stmt: AstNode.ForStatement) {
        symbols.enterBlock()
        beginLoopContext()

        stmt.init?.let { generateStatement(it) }

        val condAddress = writer.currentAddress()

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

        val updateAddress = writer.currentAddress()
        stmt.update?.let { generateStatement(it) }

        writer.addInstruction(InstructionsEnum.JMP)
        writer.addLiteral(condAddress)

        val endAddress = writer.currentAddress()
        writer.program[jmpEndPatch] = endAddress

        val continueTarget = if (stmt.update != null) updateAddress else condAddress
        endLoopContext(endAddress, continueTarget)
        symbols.exitBlock()
    }

    private fun generateExpression(expr: AstNode.Expression) {
        when (expr) {
            is AstNode.NumberLiteral -> {
                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(expr.value)
            }

            is AstNode.Identifier -> writeLoad(expr.name)

            is AstNode.ArrayAccess -> {
                emitIndexedAddress(expr.name, expr.index)
                if (resolveAddress(expr.name).isGlobal) {
                    writer.addInstruction(InstructionsEnum.GLOADI)
                } else {
                    writer.addInstruction(InstructionsEnum.LOADI)
                }
            }

            is AstNode.BinaryOp -> {
                generateExpression(expr.left)
                generateExpression(expr.right)
                emitBinaryOp(expr.op)
            }

            is AstNode.UnaryOp -> {
                generateExpression(expr.expr)
                when (expr.op) {
                    UnaryOperator.NEG -> writer.addInstruction(InstructionsEnum.NEG)
                    UnaryOperator.NOT -> writer.addInstruction(InstructionsEnum.NOT)
                    UnaryOperator.BIT_NOT -> writer.addInstruction(InstructionsEnum.B_NOT)
                }
            }

            is AstNode.TernaryOp -> {
                generateExpression(expr.condition)

                writer.addInstruction(InstructionsEnum.JIF)
                val jifPatch = writer.currentAddress()
                writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

                writer.addInstruction(InstructionsEnum.JMP)
                val jmpElsePatch = writer.currentAddress()
                writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

                val thenAddress = writer.currentAddress()
                writer.program[jifPatch] = thenAddress
                generateExpression(expr.thenExpr)

                writer.addInstruction(InstructionsEnum.JMP)
                val jmpEndPatch = writer.currentAddress()
                writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)

                val elseAddress = writer.currentAddress()
                writer.program[jmpElsePatch] = elseAddress
                generateExpression(expr.elseExpr)

                val endAddress = writer.currentAddress()
                writer.program[jmpEndPatch] = endAddress
            }

            is AstNode.FunctionCall -> {
                for (arg in expr.args) {
                    generateExpression(arg)
                }
                writer.addInstruction(InstructionsEnum.CALL)
                val patchAddress = writer.currentAddress()
                writer.addLiteral(labelResolver.UNRESOLVED_JUMP_ADDRESS)
                labelResolver.addUnresolvedLabel(expr.name, patchAddress) { "call to ${expr.name}" }
            }

            is AstNode.PreIncDec -> {
                writeLoad(expr.name)
                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(1)
                when (expr.op) {
                    IncDecOperator.INC -> writer.addInstruction(InstructionsEnum.ADD)
                    IncDecOperator.DEC -> writer.addInstruction(InstructionsEnum.SUB)
                }
                writer.addInstruction(InstructionsEnum.DUP)
                writeStore(expr.name)
            }

            is AstNode.PostIncDec -> {
                writeLoad(expr.name)
                writeLoad(expr.name)
                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(1)
                when (expr.op) {
                    IncDecOperator.INC -> writer.addInstruction(InstructionsEnum.ADD)
                    IncDecOperator.DEC -> writer.addInstruction(InstructionsEnum.SUB)
                }
                writeStore(expr.name)
            }

            is AstNode.PreIncDecArray -> {
                val addr = resolveAddress(expr.name)
                val addrSlot = allocTempSlot()
                val valueSlot = allocTempSlot()

                emitIndexedAddress(expr.name, expr.index)
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(addrSlot)

                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(addrSlot)
                if (addr.isGlobal) writer.addInstruction(InstructionsEnum.GLOADI) else writer.addInstruction(InstructionsEnum.LOADI)

                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(1)
                when (expr.op) {
                    IncDecOperator.INC -> writer.addInstruction(InstructionsEnum.ADD)
                    IncDecOperator.DEC -> writer.addInstruction(InstructionsEnum.SUB)
                }

                writer.addInstruction(InstructionsEnum.DUP)
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(valueSlot)

                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(addrSlot)
                if (addr.isGlobal) writer.addInstruction(InstructionsEnum.GSTOREI) else writer.addInstruction(InstructionsEnum.STOREI)

                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(valueSlot)
            }

            is AstNode.PostIncDecArray -> {
                val addr = resolveAddress(expr.name)
                val addrSlot = allocTempSlot()
                val valueSlot = allocTempSlot()

                emitIndexedAddress(expr.name, expr.index)
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(addrSlot)

                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(addrSlot)
                if (addr.isGlobal) writer.addInstruction(InstructionsEnum.GLOADI) else writer.addInstruction(InstructionsEnum.LOADI)

                writer.addInstruction(InstructionsEnum.DUP)
                writer.addInstruction(InstructionsEnum.STORE)
                writer.addLiteral(valueSlot)

                writer.addInstruction(InstructionsEnum.PUSH)
                writer.addLiteral(1)
                when (expr.op) {
                    IncDecOperator.INC -> writer.addInstruction(InstructionsEnum.ADD)
                    IncDecOperator.DEC -> writer.addInstruction(InstructionsEnum.SUB)
                }

                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(addrSlot)
                if (addr.isGlobal) writer.addInstruction(InstructionsEnum.GSTOREI) else writer.addInstruction(InstructionsEnum.STOREI)

                writer.addInstruction(InstructionsEnum.LOAD)
                writer.addLiteral(valueSlot)
            }

            is AstNode.AssignExpr -> {
                generateExpression(expr.value)
                writer.addInstruction(InstructionsEnum.DUP)
                writeStore(expr.name)
            }

            is AstNode.ArrayAssignExpr -> {
                generateExpression(expr.value)
                writer.addInstruction(InstructionsEnum.DUP)
                emitIndexedAddress(expr.name, expr.index)
                if (resolveAddress(expr.name).isGlobal) {
                    writer.addInstruction(InstructionsEnum.GSTOREI)
                } else {
                    writer.addInstruction(InstructionsEnum.STOREI)
                }
            }

            is AstNode.CompoundAssignExpr -> {
                writeLoad(expr.name)
                generateExpression(expr.value)
                emitBinaryOp(expr.op)
                writer.addInstruction(InstructionsEnum.DUP)
                writeStore(expr.name)
            }
        }
    }

    private fun emitBinaryOp(op: BinaryOperator) {
        when (op) {
            BinaryOperator.ADD -> writer.addInstruction(InstructionsEnum.ADD)
            BinaryOperator.SUB -> writer.addInstruction(InstructionsEnum.SUB)
            BinaryOperator.MUL -> writer.addInstruction(InstructionsEnum.MUL)
            BinaryOperator.DIV -> writer.addInstruction(InstructionsEnum.DIV)
            BinaryOperator.MOD -> writer.addInstruction(InstructionsEnum.MOD)
            BinaryOperator.EQ -> writer.addInstruction(InstructionsEnum.EQ)
            BinaryOperator.NE -> writer.addInstruction(InstructionsEnum.NE)
            BinaryOperator.LT -> writer.addInstruction(InstructionsEnum.LT)
            BinaryOperator.GT -> writer.addInstruction(InstructionsEnum.GT)
            BinaryOperator.LTE -> writer.addInstruction(InstructionsEnum.LTE)
            BinaryOperator.GTE -> writer.addInstruction(InstructionsEnum.GTE)
            BinaryOperator.BIT_AND -> writer.addInstruction(InstructionsEnum.B_AND)
            BinaryOperator.BIT_OR -> writer.addInstruction(InstructionsEnum.B_OR)
            BinaryOperator.BIT_XOR -> writer.addInstruction(InstructionsEnum.B_XOR)
            BinaryOperator.AND -> writer.addInstruction(InstructionsEnum.AND)
            BinaryOperator.OR -> writer.addInstruction(InstructionsEnum.OR)
        }
    }

    private fun allocTempSlot(): Int {
        val slot = symbols.declareLocal("__tmp${tempCounter}")
        tempCounter++
        return slot
    }

    private fun resolveAddress(name: String): AddressRef {
        symbols.resolveLocalSymbol(name)?.let { return AddressRef(it.slot, false) }
        symbols.resolveGlobalSymbol(name)?.let { return AddressRef(it.slot, true) }
        throw CodeGenException("Undefined variable '$name'")
    }

    private fun emitIndexedAddress(name: String, index: AstNode.Expression) {
        val address = resolveAddress(name)
        generateExpression(index)
        writer.addInstruction(InstructionsEnum.PUSH)
        writer.addLiteral(address.baseSlot)
        writer.addInstruction(InstructionsEnum.ADD)
    }

    private fun writeLoad(name: String) {
        symbols.resolveLocal(name)?.let {
            writer.addInstruction(InstructionsEnum.LOAD)
            writer.addLiteral(it)
            return
        }
        symbols.resolveGlobal(name)?.let {
            writer.addInstruction(InstructionsEnum.GLOAD)
            writer.addLiteral(it)
            return
        }
        throw CodeGenException("Undefined variable '$name'")
    }

    private fun writeStore(name: String) {
        symbols.resolveLocal(name)?.let {
            writer.addInstruction(InstructionsEnum.STORE)
            writer.addLiteral(it)
            return
        }
        symbols.resolveGlobal(name)?.let {
            writer.addInstruction(InstructionsEnum.GSTORE)
            writer.addLiteral(it)
            return
        }
        throw CodeGenException("Undefined variable '$name'")
    }
}
