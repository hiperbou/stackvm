package com.hiperbou.vm

import generated.antlr.hiperbou.vm.vmBaseVisitor
import generated.antlr.hiperbou.vm.vmLexer
import generated.antlr.hiperbou.vm.vmParser
import generated.antlr.hiperbou.vm.vmParser.*
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.TerminalNode

class ProgramVisitor : vmBaseVisitor<Unit>() {
    private class UnresolvedAddress(val label: String, val position: Int)

    private val program: MutableList<Int> = ArrayList()
    private val labelsToResolve: MutableList<UnresolvedAddress> = ArrayList()
    private val labelsAddresses: MutableMap<String, Int> = HashMap()
    private val currentAddress: Int
        get() = program.size

    override fun visitLabel(ctx: LabelContext) {
        // When a label is found, saves the current address for later
        val labelText = ctx.IDENTIFIER().text
        labelsAddresses[labelText] = currentAddress
    }

    override fun visitNop(ctx: NopContext) {
        program.add(Instructions.NOP)
    }

    override fun visitHalt(ctx: HaltContext) {
        // When a HALT instruction is found, adds a HALT instruction to the program
        program.add(Instructions.HALT)
    }

    override fun visitPush(ctx: PushContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.PUSH)
    }

    override fun visitPop(ctx: PopContext) {
        program.add(Instructions.POP)
    }

    override fun visitDup(ctx: DupContext) {
        program.add(Instructions.DUP)
    }

    override fun visitAdd(ctx: AddContext) {
        program.add(Instructions.ADD)
    }

    override fun visitSub(ctx: SubContext) {
        program.add(Instructions.SUB)
    }

    override fun visitMul(ctx: MulContext) {
        program.add(Instructions.MUL)
    }

    override fun visitDiv(ctx: DivContext) {
        program.add(Instructions.DIV)
    }

    override fun visitMod(ctx: ModContext) {
        program.add(Instructions.MOD)
    }

    override fun visitMin(ctx: MinContext) {
        program.add(Instructions.MIN)
    }

    override fun visitMax(ctx: MaxContext) {
        program.add(Instructions.MAX)
    }

    override fun visitNot(ctx: NotContext) {
        program.add(Instructions.NOT)
    }

    override fun visitB_not(ctx: B_notContext) {
        program.add(Instructions.B_NOT)
    }

    override fun visitAbs(ctx: AbsContext) {
        program.add(Instructions.ABS)
    }

    override fun visitAnd(ctx: AndContext) {
        program.add(Instructions.AND)
    }

    override fun visitOr(ctx: OrContext) {
        program.add(Instructions.OR)
    }

    override fun visitB_and(ctx: B_andContext) {
        program.add(Instructions.B_AND)
    }

    override fun visitB_or(ctx: B_orContext) {
        program.add(Instructions.B_OR)
    }

    override fun visitB_xor(ctx: B_xorContext) {
        program.add(Instructions.B_XOR)
    }

    override fun visitEq(ctx: EqContext) {
        program.add(Instructions.EQ)
    }

    override fun visitNe(ctx: NeContext) {
        program.add(Instructions.NE)
    }

    override fun visitGte(ctx: GteContext) {
        program.add(Instructions.GTE)
    }

    override fun visitLte(ctx: LteContext) {
        program.add(Instructions.LTE)
    }

    override fun visitGt(ctx: GtContext) {
        program.add(Instructions.GT)
    }

    override fun visitLt(ctx: LtContext) {
        program.add(Instructions.LT)
    }

    override fun visitJmp(ctx: JmpContext) {
        visitUnresolvedJump(ctx.IDENTIFIER(), Instructions.JMP)
    }

    override fun visitJif(ctx: JifContext) {
        visitUnresolvedJump(ctx.IDENTIFIER(), Instructions.JIF)
    }

    override fun visitLoad(ctx: LoadContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.LOAD)
    }

    override fun visitStore(ctx: StoreContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.STORE)
    }

    override fun visitGload(ctx: GloadContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.GLOAD)
    }

    override fun visitGstore(ctx: GstoreContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.GSTORE)
    }

    override fun visitRead(ctx: ReadContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.READ)
    }

    override fun visitWrite(ctx: WriteContext) {
        visitOneArgumentInstruction(ctx.NUMBER(), Instructions.WRITE)
    }

    override fun visitCall(ctx: CallContext) {
        visitUnresolvedJump(ctx.IDENTIFIER(), Instructions.CALL)
    }

    override fun visitRet(ctx: RetContext) {
        program.add(Instructions.RET)
    }

    private fun visitOneArgumentInstruction(numer: TerminalNode, instruction: Int) {
        val value = Integer.valueOf(numer.text)
        program.add(instruction)
        program.add(value)
    }

    private fun visitUnresolvedJump(identifier: TerminalNode, instruction: Int) {
        // Add the given instruction, save the unresolved label and add a placeholder for the jump address
        program.add(instruction)
        val labelText = identifier.text
        labelsToResolve.add(UnresolvedAddress(labelText, currentAddress))
        program.add(UNRESOLVED_JUMP_ADDRESS)
    }

    /**
     * Returns the current program in a format suitable for the CPU execution.
     */
    fun generateProgram(): IntArray {
        resolveLabels()

        // Horrible code to convert a List<Integer> to an int[]
        val result = IntArray(program.size)
        for (i in program.indices) {
            result[i] = program[i]
        }
        return result
    }

    /**
     * Transforms all the unresolved labels into correct addresses.
     */
    private fun resolveLabels() {
        for (unresolvedAddress in labelsToResolve) {
            // Map the jump to its real address, by checking the label's address
            val destination = labelsAddresses[unresolvedAddress.label]
                ?: throw InvalidProgramException("Unresolved label " + unresolvedAddress.label)
            assert(program[unresolvedAddress.position] == UNRESOLVED_JUMP_ADDRESS)
            program[unresolvedAddress.position] = destination
        }

        // Clean up
        labelsToResolve.clear()
    }

    companion object {
        private const val UNRESOLVED_JUMP_ADDRESS = Int.MIN_VALUE

        /**
         * Generates a program from a given parser, or throws an exception if the program is invalid.
         */
        @Throws(InvalidProgramException::class)
        fun generateProgram(parser: vmParser): IntArray {
            val programVisitor = ProgramVisitor()
            programVisitor.visit(parser.program())
            return programVisitor.generateProgram()
        }

        /**
         * Generates a program from a given ANTLR input, or throws an exception if the program is invalid.
         */
        @Throws(InvalidProgramException::class)
        fun generateProgram(input: CharStream?): IntArray {
            val lexer = vmLexer(input)
            val tokenStream = CommonTokenStream(lexer)
            val parser = vmParser(tokenStream)
            return generateProgram(parser)
        }
    }
}