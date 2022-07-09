package com.hiperbou.vm.decoder

import com.hiperbou.vm.*
import com.hiperbou.vm.Instructions.ABS
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
import com.hiperbou.vm.Instructions.GLOAD
import com.hiperbou.vm.Instructions.GSTORE
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
import com.hiperbou.vm.Instructions.READ
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.WRITE
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CoreDecoder(private val cpu: CPU, private val stack: CPUStack<Int>, private val frames: CPUFrames<Frame>, private var nextDecoder: Decoder = ExceptionDecoder.instance):
    Decoder {
    override fun decodeInstruction(instruction: Int) { with(stack) { with(cpu) {
        when (instruction) {
            HALT -> haltCPU()
            PUSH -> {
                // The word after the instruction will contain the value to push
                val value = getNextWordFromProgram("Should have the value after the PUSH instruction")
                push(value)
            }
            POP -> {
                checkIsNotEmpty("POP")
                pop()
            }
            DUP -> {
                checkIsNotEmpty("DUP")
                val n: Int = peek()
                push(n)
            }
            LOAD -> {
                val varNumber =
                    getNextWordFromProgram("Should have the variable number after the LOAD instruction")
                push(getCurrentFrame().getVariable(varNumber))
            }
            STORE -> {
                val varNumber =
                    getNextWordFromProgram("Should have the variable number after the STORE instruction")
                checkIsNotEmpty("STORE")
                getCurrentFrame().setVariable(varNumber, pop())
            }
            GLOAD -> {
                val varNumber =
                    getNextWordFromProgram("Should have the variable number after the GLOAD instruction")
                push(getGlobals().getVariable(varNumber))
            }
            GSTORE -> {
                val varNumber =
                    getNextWordFromProgram("Should have the variable number after the GSTORE instruction")
                checkIsNotEmpty("GSTORE")
                getGlobals().setVariable(varNumber, pop())
            }
            READ -> {
                val varNumber =
                    getNextWordFromProgram("Should have the memory index after the READ instruction")
                push(getMemory().get(varNumber))
            }
            WRITE -> {
                val index =
                    getNextWordFromProgram("Should have the memory index after the WRITE instruction")
                checkIsNotEmpty("WRITE")
                getMemory().set(index, pop())
            }
            NOT -> {
                checkIsNotEmpty("NOT")
                push((!(pop().toBool())).toInt())
            }
            B_NOT -> {
                checkIsNotEmpty("B_NOT")
                push(pop().inv())
            }
            ABS -> {
                checkIsNotEmpty("ABS")
                push(abs(pop()))
            }
            ADD, SUB, MUL, DIV, MOD, MIN, MAX, AND, OR, B_AND, B_OR, B_XOR, EQ, NE, GTE, LTE, GT, LT -> {
                checkAtLeast2Items()
                val n2 = pop()
                val n1 = pop()
                push(evaluateBinaryOperation(instruction, n1, n2))
            }
            JMP -> {
                // The word after the instruction will contain the address to jump to
                val address = getNextWordFromProgram("Should have the address after the JMP instruction")
                checkJumpAddress(address)
                instructionAddress = address
            }
            JIF -> {
                // The word after the instruction will contain the address to jump to
                val address = getNextWordFromProgram("Should have the address after the JIF instruction")
                checkJumpAddress(address)
                checkIsNotEmpty("JIF")
                if (pop().toBool()) instructionAddress = address
                UInt
            }
            CALL -> {
                // The word after the instruction will contain the function address
                val address = getNextWordFromProgram("Should have the address after the CALL instruction")
                checkJumpAddress(address)
                frames.push(Frame(instructionAddress)) // Push a new stack frame
                instructionAddress = address // and jump!
            }
            RET -> {
                // Pop the stack frame and return to the previous address
                frames.checkThereIsAReturnAddress(instructionAddress)
                instructionAddress = frames.pop().returnAddress
            }
            NOP -> {}
            else -> nextDecoder.decodeInstruction(instruction)
        }}}
    }

    private fun evaluateBinaryOperation(instruction: Int, n1: Int, n2: Int): Int {

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

    override fun setNextDecoder(decoder: Decoder) {
        nextDecoder = decoder
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