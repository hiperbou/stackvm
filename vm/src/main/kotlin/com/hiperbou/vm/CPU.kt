package com.hiperbou.vm

import com.hiperbou.vm.Instructions.ABS
import kotlin.collections.ArrayDeque
import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.AND
import com.hiperbou.vm.Instructions.B_AND
import com.hiperbou.vm.Instructions.B_NOT
import com.hiperbou.vm.Instructions.B_OR
import com.hiperbou.vm.Instructions.B_XOR
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.DIV
import com.hiperbou.vm.Instructions.DUP
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.EQ
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.LTE
import com.hiperbou.vm.Instructions.LT
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.MAX
import com.hiperbou.vm.Instructions.MIN
import com.hiperbou.vm.Instructions.MOD
import com.hiperbou.vm.Instructions.MUL
import com.hiperbou.vm.Instructions.NE
import com.hiperbou.vm.Instructions.NOP
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CPU(vararg instructions:Int) {

    private val program: IntArray = instructions

    private var instructionAddress = 0
    private var halted = false

    private val stack = ArrayDeque<Int>()
    private val frames = ArrayDeque<Frame>()

    private fun <T> ArrayDeque<T>.push(element:T) = addFirst(element)
    private fun <T> ArrayDeque<T>.pop():T = removeFirst()
    private fun <T> ArrayDeque<T>.peek():T = first()

    init {
        assert(program.isNotEmpty()) { "A program should have at least an instruction" }
        frames.push(Frame(0)) // Prepare the initial frame
    }

    fun getInstructionAddress() = instructionAddress
    fun isHalted() = halted
    fun getStack() = stack

    fun run() {
        while (!halted) {
            step()
        }
    }

    fun step() {
        assert(!halted) { "A halted CPU cannot execute the program" }
        val nextInstruction: Int = getNextWordFromProgram { "Should have a next instruction" }
        decodeInstruction(nextInstruction)
    }


    private fun checkJumpAddress(address: Int) {
        if (address < 0 || address >= program.size) {
            throw InvalidProgramException("Invalid jump address $address at $instructionAddress")
        }
    }

    private fun checkThereIsAReturnAddress() {
        if (frames.size == 1) throw InvalidProgramException("Invalid RET instruction: no current function call $instructionAddress")
    }

    private fun checkStackHasAtLeastOneItem(lazyInstructionName: ()->String) {
        if (stack.size < 1) throw InvalidProgramException("There should be at least one item on the stack to execute an {$lazyInstructionName()} instruction")

    }

    private fun decodeInstruction(instruction: Int) {
        when (instruction) {
            HALT -> halted = true
            PUSH -> {
                // The word after the instruction will contain the value to push
                val value = getNextWordFromProgram { "Should have the value after the PUSH instruction" }
                stack.push(value)
            }
            POP -> {
                checkStackHasAtLeastOneItem { "POP" }
                stack.pop()
            }
            DUP -> {
                checkStackHasAtLeastOneItem { "DUP" }
                val n: Int = stack.peek()
                stack.push(n)
            }
            LOAD -> {
                val varNumber = getNextWordFromProgram { "Should have the variable number after the LOAD instruction" }
                stack.push(getCurrentFrame().getVariable(varNumber))
            }
            STORE -> {
                val varNumber = getNextWordFromProgram { "Should have the variable number after the STORE instruction" }
                checkStackHasAtLeastOneItem { "STORE" }
                getCurrentFrame().setVariable(varNumber, stack.pop())
            }
            NOT -> {
                checkStackHasAtLeastOneItem { "NOT" }
                stack.push((!(stack.pop().toBool())).toInt())
            }
            B_NOT -> {
                checkStackHasAtLeastOneItem { "B_NOT" }
                stack.push(stack.pop().inv())
            }
            ABS -> {
                checkStackHasAtLeastOneItem { "ABS" }
                stack.push(abs(stack.pop()))
            }
            ADD, SUB, MUL, DIV, MOD, MIN, MAX, AND, OR, B_AND, B_OR, B_XOR, EQ, NE, GTE, LTE, GT, LT -> {
                if (stack.size < 2) {
                    throw InvalidProgramException("There should be at least two items on the stack to execute a binary instruction")
                }
                val n2: Int = stack.pop()
                val n1: Int = stack.pop()
                stack.push(doBinaryOp(instruction, n1, n2))
            }
            JMP -> {
                // The word after the instruction will contain the address to jump to
                val address = getNextWordFromProgram { "Should have the address after the JMP instruction" }
                checkJumpAddress(address)
                instructionAddress = address
            }
            JIF -> {
                // The word after the instruction will contain the address to jump to
                val address = getNextWordFromProgram { "Should have the address after the JIF instruction" }
                checkJumpAddress(address)
                checkStackHasAtLeastOneItem { "JIF" }
                if (stack.pop().toBool()) {
                    instructionAddress = address
                }
            }
            CALL -> {
                // The word after the instruction will contain the function address
                val address = getNextWordFromProgram { "Should have the address after the CALL instruction" }
                checkJumpAddress(address)
                frames.push(Frame(instructionAddress)) // Push a new stack frame
                instructionAddress = address // and jump!
            }
            RET -> {
                // Pop the stack frame and return to the previous address
                checkThereIsAReturnAddress()
                instructionAddress = frames.pop().returnAddress
            }
            NOP -> {}
            else -> throw InvalidProgramException("Unknown instruction: $instruction")
        }
    }

    private fun doBinaryOp(instruction: Int, n1: Int, n2: Int): Int {

        return when (instruction) {
            ADD -> n1 + n2
            SUB -> n1 - n2
            MUL -> n1 * n2
            DIV -> n1 / n2
            MOD -> n1.umod(n2)
            MIN -> min(n1, n2)
            MAX -> max(n1, n2)
            AND -> (n1.toBool() && n2.toBool()).toInt()
            OR -> (n1.toBool() || n2.toBool()).toInt()
            B_AND -> n1 and n2
            B_OR -> n1 or n2
            B_XOR -> n1 xor n2
            EQ -> (n1 == n2).toInt()
            NE -> (n1 != n2).toInt()
            GTE -> (n1 >= n2).toInt()
            LTE -> (n1 <= n2).toInt()
            GT -> (n1 > n2).toInt()
            LT -> (n1 < n2).toInt()
            else -> throw AssertionError()
        }
    }

    private fun getNextWordFromProgram(lazyErrorMessage: ()-> String): Int {
        if (instructionAddress >= program.size) {
            throw InvalidProgramException(lazyErrorMessage())
        }
        val nextWord = program[instructionAddress]
        ++instructionAddress
        return nextWord
    }

    fun getCurrentFrame(): Frame {
        return frames.peek()
    }

    private fun Int.toBool() = this != 0
    private fun Boolean.toInt() = if (this) 1 else 0

    private infix fun Int.umod(other: Int): Int {
        val rm = this % other
        val remainder = if (rm == -0) 0 else rm
        return when {
            remainder < 0 -> remainder + abs(other)
            else -> remainder
        }
    }
}