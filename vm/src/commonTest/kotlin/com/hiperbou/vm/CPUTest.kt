package com.hiperbou.vm

import com.hiperbou.vm.Instructions.ABS
import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.AND
import com.hiperbou.vm.Instructions.B_AND
import com.hiperbou.vm.Instructions.B_NOT
import com.hiperbou.vm.Instructions.B_OR
import com.hiperbou.vm.Instructions.B_XOR
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.CALLI
import com.hiperbou.vm.Instructions.DIV
import com.hiperbou.vm.Instructions.DUP
import com.hiperbou.vm.Instructions.EQ
import com.hiperbou.vm.Instructions.GLOAD
import com.hiperbou.vm.Instructions.GLOADI
import com.hiperbou.vm.Instructions.GSTORE
import com.hiperbou.vm.Instructions.GSTOREI
import com.hiperbou.vm.Instructions.GT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.LOADI
import com.hiperbou.vm.Instructions.LT
import com.hiperbou.vm.Instructions.LTE
import com.hiperbou.vm.Instructions.MAX
import com.hiperbou.vm.Instructions.MIN
import com.hiperbou.vm.Instructions.MOD
import com.hiperbou.vm.Instructions.MUL
import com.hiperbou.vm.Instructions.NE
import com.hiperbou.vm.Instructions.NEG
import com.hiperbou.vm.Instructions.NOT
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.READ
import com.hiperbou.vm.Instructions.READI
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.STOREI
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.WRITE
import com.hiperbou.vm.Instructions.WRITEI
import com.hiperbou.vm.plugin.conditional.ConditionalDecoder
import com.hiperbou.vm.plugin.conditional.ConditionalInstructions.BRANCH_TABLE
import com.hiperbou.vm.plugin.conditional.ConditionalInstructions.SELECT
import com.hiperbou.vm.plugin.pointer.PointerDecoder
import com.hiperbou.vm.plugin.pointer.PointerInstructions.ADDR_OF
import com.hiperbou.vm.plugin.pointer.PointerInstructions.DEREF
import com.hiperbou.vm.plugin.pointer.PointerInstructions.PTR_ADD
import com.hiperbou.vm.plugin.pointer.PointerInstructions.PTR_DIFF
import com.hiperbou.vm.plugin.pointer.PointerInstructions.PTR_SUB
import kotlin.test.*

class CPUTest {

    @Test
    fun testEmptyProgramFails() {
        assertFailsWith(AssertionError::class) {
            val cpu = CPU()
            cpu.run()
        }
    }

    @Test
    fun testUnknownInstructionFails() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(-1, HALT)
            cpu.run()
        }
    }

    @Test
    fun testEmptyProgramDoesNothing() {
        val cpu = CPU(HALT)
        cpu.step()
        assertEquals(1, cpu.instructionAddress)
        assertTrue(cpu.isHalted())
        assertStackIsEmpty(cpu)
    }


    @Test
    fun testPushAndThenHalt() {
        val cpu = CPU(PUSH, 42, HALT)
        cpu.step()
        assertEquals(2, cpu.instructionAddress)
        assertFalse(cpu.isHalted())
        assertStackContains(cpu, 42)
        cpu.step()
        assertTrue(cpu.isHalted())
    }

    @Test
    fun testPushPushAndThenHalt() {
        val cpu = CPU(PUSH, 42, PUSH, 68, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 5)
        assertStackContains(cpu, 68, 42)
    }

    @Test
    fun testPushShouldBeFollowedByAWord() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH)
            cpu.step()
        }
    }

    @Test
    fun testPop() {
        val cpu = CPU(PUSH, 42, POP, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackIsEmpty(cpu)
    }

    @Test
    fun testPopNeedsAnItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(POP)
            cpu.step()
        }
    }

    @Test
    fun testDup() {
        val cpu = CPU(PUSH, 42, DUP, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 42, 42)
    }

    @Test
    fun testDupNeedsAnItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(DUP)
            cpu.step()
        }
    }

    @Test
    fun testAddTwoNumbers() {
        val cpu = CPU(PUSH, 1, PUSH, 2, ADD, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 3)
    }

    @Test
    fun testAddNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(ADD, HALT)
            cpu.run()
        }
    }

    @Test
    fun testSubTwoNumbers() {
        val cpu = CPU(PUSH, 1, PUSH, 2, SUB, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, -1)
    }

    @Test
    fun testSubNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(SUB, HALT)
            cpu.run()
        }
    }

    @Test
    fun testMulTwoNumbers() {
        val cpu = CPU(PUSH, 2, PUSH, 5, MUL, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 10)
    }

    @Test
    fun testMulNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(MUL, HALT)
            cpu.run()
        }
    }

    @Test
    fun testDivTwoNumbers() {
        val cpu = CPU(PUSH, 8, PUSH, 2, DIV, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 4)
    }

    @Test
    fun testDivNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(DIV, HALT)
            cpu.run()
        }
    }

    @Test
    fun testModTwoNumbers() {
        val cpu = CPU(PUSH, 8, PUSH, 2, MOD, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testModTwoNumbers2() {
        val cpu = CPU(PUSH, 7, PUSH, 2, MOD, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testModTwoNumbersNegative() {
        val cpu = CPU(PUSH, -8, PUSH, 2, MOD, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testModTwoNumbersNegative2() {
        val cpu = CPU(PUSH, -7, PUSH, 2, MOD, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testModNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(MOD, HALT)
            cpu.run()
        }
    }

    @Test
    fun testMinTwoNumbers() {
        val cpu = CPU(PUSH, 8, PUSH, 2, MIN, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 2)
    }

    @Test
    fun testMinNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(MIN, HALT)
            cpu.run()
        }
    }

    @Test
    fun testMaxTwoNumbers() {
        val cpu = CPU(PUSH, 8, PUSH, 2, MAX, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 8)
    }

    @Test
    fun testMaxNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(MAX, HALT)
            cpu.run()
        }
    }

    @Test
    fun testUnaryNotTrue() {
        val cpu = CPU(PUSH, 1, NOT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testUnaryNotFalse() {
        val cpu = CPU(PUSH, 0, NOT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testNotNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(NOT, HALT)
            cpu.run()
        }
    }

    @Test
    fun testUnaryBinaryNotTrue() {
        val cpu = CPU(PUSH, 0xff0f, B_NOT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 0xffff00f0.toInt())
    }

    @Test
    fun testBinaryNotNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(B_NOT, HALT)
            cpu.run()
        }
    }

    @Test
    fun testUnaryAbs() {
        val cpu = CPU(PUSH, -1, ABS, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testUnaryAbs2() {
        val cpu = CPU(PUSH, 1, ABS, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testAbsNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(ABS, HALT)
            cpu.run()
        }
    }

    @Test
    fun testUnaryNeg() {
        val cpu = CPU(PUSH, -1, NEG, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testUnaryNeg2() {
        val cpu = CPU(PUSH, 1, NEG, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, -1)
    }

    @Test
    fun testNegNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(NEG, HALT)
            cpu.run()
        }
    }


    @Test
    fun testAndTrueTrue() {
        val cpu = CPU(PUSH, 1, PUSH, 1, AND, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testAndNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(AND, HALT)
            cpu.run()
        }
    }

    @Test
    fun testOrTrueFalse() {
        val cpu = CPU(PUSH, 1, PUSH, 0, OR, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testOrNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(OR, HALT)
            cpu.run()
        }
    }

    @Test
    fun testBinaryAndTrueFalse() {
        val cpu = CPU(PUSH, 0x0F, PUSH, 0xF0, B_AND, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0x00)
    }

    @Test
    fun testBinaryAndNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(B_AND, HALT)
            cpu.run()
        }
    }

    @Test
    fun testBinaryOrTrueFalse() {
        val cpu = CPU(PUSH, 0xF0, PUSH, 0x0F, B_OR, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0xFF)
    }

    @Test
    fun testBinaryOrNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(B_OR, HALT)
            cpu.run()
        }
    }

    @Test
    fun testBinaryXOrTrueFalse() {
        val cpu = CPU(PUSH, 0xF0, PUSH, 0xFF, B_XOR, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0x0F)
    }

    @Test
    fun testBinaryXOrNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(B_XOR, HALT)
            cpu.run()
        }
    }

    @Test
    fun testIsEqualsFalse() {
        val cpu = CPU(PUSH, 8, PUSH, 2, EQ, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testIsEqualsTrue() {
        val cpu = CPU(PUSH, 2, PUSH, 2, EQ, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testIsEqualsNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(EQ, HALT)
            cpu.run()
        }
    }

    @Test
    fun testIsNotEqualsTrue() {
        val cpu = CPU(PUSH, 8, PUSH, 2, NE, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testIsNotEqualsFalse() {
        val cpu = CPU(PUSH, 2, PUSH, 2, NE, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testIsNotEqualsNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(NE, HALT)
            cpu.run()
        }
    }

    @Test
    fun testIsGreaterEqualsTrue() {
        val cpu = CPU(PUSH, 3, PUSH, 2, GTE, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testIsGreaterEqualsFalse() {
        val cpu = CPU(PUSH, 1, PUSH, 2, GTE, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testIsGreaterEqualsNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GTE, HALT)
            cpu.run()
        }
    }

    @Test
    fun testIsLessEqualsTrue() {
        val cpu = CPU(PUSH, 3, PUSH, 2, LTE, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testIsLessEqualsFalse() {
        val cpu = CPU(PUSH, 1, PUSH, 2, LTE, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testIsLessEqualsNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(LTE, HALT)
            cpu.run()
        }
    }

    @Test
    fun testIsGreaterThanFalse() {
        val cpu = CPU(PUSH, 1, PUSH, 2, GT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testIsGreaterThanTrue() {
        val cpu = CPU(PUSH, 3, PUSH, 2, GT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testIsGreaterThanNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GT, HALT)
            cpu.run()
        }
    }

    @Test
    fun testIsLessThanTrue() {
        val cpu = CPU(PUSH, 2, PUSH, 3, LT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 1)
    }

    @Test
    fun testIsLessThanFalse() {
        val cpu = CPU(PUSH, 3, PUSH, 2, LT, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testIsLessThanNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(LT, HALT)
            cpu.run()
        }
    }


    @Test
    fun testUnconditionalJump() {
        // address:     0  1     2    3  4
        val cpu = CPU(JMP, 3, HALT, JMP, 2)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
    }

    @Test
    fun testJumpNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(JMP)
            cpu.run()
        }
    }

    @Test
    fun testJumpInvalidAddress() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(JMP, 3, HALT)
            cpu.run()
        }
    }

    @Test
    fun testConditionalJump() {
        // address:      0  1    2  3    4     5  6    7  8     9
        val cpu = CPU(PUSH, 1, JIF, 5, POP, PUSH, 0, JIF, 4, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 10)
    }

    @Test
    fun testConditionalJumpNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 1, JIF)
            cpu.run()
        }
    }

    @Test
    fun testJumpNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(JIF, 0, HALT)
            cpu.run()
        }
    }

    @Test
    fun testLoadIndirectVariableNotInitialized() {
        val cpu = CPU(PUSH, 0, LOADI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testStoreIndirectVariable() {
        val cpu = CPU(PUSH, 42, PUSH, 0, STOREI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackIsEmpty(cpu)
        assertVariableValues(cpu, 42)
    }

    @Test
    fun testStoreAndLoadIndirectVariable() {
        val cpu = CPU(PUSH, 42, PUSH, 0, STOREI, PUSH, 0, LOADI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 9)
        assertStackContains(cpu, 42)
        assertVariableValues(cpu, 42)
    }

    @Test
    fun testLoadIndirectNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(LOADI, HALT)
            cpu.run()
        }
    }
    @Test
    fun testStoreIndirectNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(STOREI, HALT)
            cpu.run()
        }
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 0, STOREI, HALT)
            cpu.run()
        }
    }

    @Test
    fun testGLoadIndirectVariableNotInitialized() {
        val cpu = CPU(PUSH, 0, GLOADI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testGStoreIndirectVariable() {
        val cpu = CPU(PUSH, 42, PUSH, 0, GSTOREI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackIsEmpty(cpu)
        assertGlobalVariableValues(cpu, 42)
    }

    @Test
    fun testGStoreAndGLoadIndirectVariable() {
        val cpu = CPU(PUSH, 42, PUSH, 0, GSTOREI, PUSH, 0, GLOADI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 9)
        assertStackContains(cpu, 42)
        assertGlobalVariableValues(cpu, 42)
    }

    @Test
    fun testGLoadIndirectNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GSTOREI, HALT)
            cpu.run()
        }
    }

    @Test
    fun testGStoreNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GSTOREI, HALT)
            cpu.run()
        }
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 0, GSTOREI, HALT)
            cpu.run()
        }
    }

    @Test
    fun testReadMemoryIndirectNotInitialized() {
        val cpu = CPU(PUSH, 0, READI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testWriteIndirectMemory() {
        val cpu = CPU(PUSH, 42, PUSH, 0, WRITEI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackIsEmpty(cpu)
        assertMemoryValues(cpu, 42)
    }

    @Test
    fun testWriteAndReadIndirectMemory() {
        val cpu = CPU(PUSH, 42, PUSH, 0, WRITEI, PUSH, 0, READI, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 9)
        assertStackContains(cpu, 42)
        assertMemoryValues(cpu, 42)
    }

    @Test
    fun testReadIndirectNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(READI, HALT)
            cpu.run()
        }
    }


    @Test
    fun testWriteNeedsTwoItemsOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(WRITEI, HALT)
            cpu.run()
        }
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 0, WRITEI, HALT)
            cpu.run()
        }
    }

    @Test
    fun testLoadVariableNotInitialized() {
        val cpu = CPU(LOAD, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testStoreVariable() {
        val cpu = CPU(PUSH, 42, STORE, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 5)
        assertStackIsEmpty(cpu)
        assertVariableValues(cpu, 42)
    }

    @Test
    fun testStoreAndLoadVariable() {
        val cpu = CPU(PUSH, 42, STORE, 0, LOAD, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 7)
        assertStackContains(cpu, 42)
        assertVariableValues(cpu, 42)
    }

    @Test
    fun testLoadNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(LOAD)
            cpu.run()
        }
    }

    @Test
    fun testStoreNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(STORE)
            cpu.run()
        }
    }

    @Test
    fun testStoreNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(STORE, 0, HALT)
            cpu.run()
        }
    }

    @Test
    fun testGLoadVariableNotInitialized() {
        val cpu = CPU(GLOAD, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testGStoreVariable() {
        val cpu = CPU(PUSH, 42, GSTORE, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 5)
        assertStackIsEmpty(cpu)
        assertGlobalVariableValues(cpu, 42)
    }

    @Test
    fun testGStoreAndGLoadVariable() {
        val cpu = CPU(PUSH, 42, GSTORE, 0, GLOAD, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 7)
        assertStackContains(cpu, 42)
        assertGlobalVariableValues(cpu, 42)
    }

    @Test
    fun testGLoadNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GLOAD)
            cpu.run()
        }
    }

    @Test
    fun testGStoreNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GSTORE)
            cpu.run()
        }
    }

    @Test
    fun testGStoreNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(GSTORE, 0, HALT)
            cpu.run()
        }
    }

    @Test
    fun testReadMemoryNotInitialized() {
        val cpu = CPU(READ, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
        assertStackContains(cpu, 0)
    }

    @Test
    fun testWriteMemory() {
        val cpu = CPU(PUSH, 42, WRITE, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 5)
        assertStackIsEmpty(cpu)
        assertMemoryValues(cpu, 42)
    }

    @Test
    fun testWriteAndReadMemory() {
        val cpu = CPU(PUSH, 42, WRITE, 0, READ, 0, HALT)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 7)
        assertStackContains(cpu, 42)
        assertMemoryValues(cpu, 42)
    }

    @Test
    fun testReadNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(READ)
            cpu.run()
        }
    }

    @Test
    fun testWriteNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(WRITE)
            cpu.run()
        }
    }

    @Test
    fun testWriteNeedsOneItemOnTheStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(WRITE, 0, HALT)
            cpu.run()
        }
    }


    @Test
    fun testFunctionCallNoArgumentsNoReturn() {
        // addresses      0     1  2     3
        val cpu = CPU(CALL, 3, HALT, RET)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
        assertStackIsEmpty(cpu)
    }

    @Test
    fun testFunctionCallIndirectNoArgumentsNoReturn() {
        // addresses      0     1  2     3
        val cpu = CPU(PUSH, 3, CALLI, HALT, RET)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackIsEmpty(cpu)
    }

    @Test
    fun testReturnToNowhereFails() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(RET, HALT)
            cpu.run()
        }
    }


    @Test
    fun testFunctionCallNoArgumentsReturnsInt() {
        // addresses      0     1  2     3     4  5
        val cpu = CPU(CALL, 3, HALT, PUSH, 7, RET)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
        assertStackContains(cpu, 7)
    }

    @Test
    fun testFunctionDoublesGivenArgument() {
        // addresses      0     1  2      3  4    5     6  7     8  9    10
        val cpu = CPU(PUSH, 3, CALL, 5, HALT, PUSH, 2, MUL, RET)
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 5)
        assertStackContains(cpu, 6)
    }

    @Test
    fun testSelectTrue() {
        val cpu = CPU(PUSH, 1, PUSH, 42, PUSH, 99, SELECT, HALT)
        cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 8)
        assertStackContains(cpu, 42) // Should select value_if_true
    }

    @Test
    fun testSelectFalse() {
        val cpu = CPU(PUSH, 0, PUSH, 42, PUSH, 99, SELECT, HALT)
        cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 8)
        assertStackContains(cpu, 99) // Should select value_if_false
    }

    @Test
    fun testSelectNeedsThreeItemsOnStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 1, PUSH, 42, SELECT, HALT)
            cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testBranchTableValidIndex() {
        // BRANCH_TABLE with 3 entries, index 1 should jump to address 10
        val cpu = CPU(
            PUSH, 1,           // addresses 0-1: index = 1
            BRANCH_TABLE, 3,   // addresses 2-3: table size = 3
            8, 10, 14,         // addresses 4-6: jump table: [8, 10, 14]
            PUSH, 99,          // addresses 7-8: should be skipped
            HALT,              // address 9
            PUSH, 42,          // addresses 10-11: target for index 1
            HALT               // address 12
        )
        cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 13)
        assertStackContains(cpu, 42)
    }

    @Test
    fun testBranchTableInvalidIndex() {
        // Index out of bounds should continue execution after table
        val cpu = CPU(
            PUSH, 5,           // index = 5 (out of bounds)
            BRANCH_TABLE, 3,   // table size = 3
            8, 10, 12,         // jump table entries
            PUSH, 42,          // continues here after skipping table
            HALT
        )
        cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 10)
        assertStackContains(cpu, 42)
    }

    @Test
    fun testBranchTableNegativeIndex() {
        val cpu = CPU(
            PUSH, -1,          // addresses 0-1: negative index
            BRANCH_TABLE, 2,   // addresses 2-3: table size = 2
            6, 8,              // addresses 4-5: jump table
            PUSH, 99,          // addresses 6-7: continues here
            HALT               // address 8
        )
        cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 9)
        assertStackContains(cpu, 99)
    }

    @Test
    fun testBranchTableNeedsOneItemOnStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(BRANCH_TABLE, 2, 4, 6, HALT)
            cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testBranchTableInvalidJumpAddress() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(
                PUSH, 0,
                BRANCH_TABLE, 1,
                999, // invalid jump address
                HALT
            )
            cpu.appendDecoder(ConditionalDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testPtrAdd() {
        val cpu = CPU(PUSH, 100, PUSH, 5, PTR_ADD, HALT)
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 105) // 100 + 5
    }

    @Test
    fun testPtrSub() {
        val cpu = CPU(PUSH, 100, PUSH, 5, PTR_SUB, HALT)
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 95) // 100 - 5
    }

    @Test
    fun testPtrAddNeedsTwoItemsOnStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 100, PTR_ADD, HALT)
            cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testPtrDiff() {
        val cpu = CPU(PUSH, 200, PUSH, 100, PTR_DIFF, HALT)
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 100) // 200 - 100
    }

    @Test
    fun testPtrDiffNeedsTwoItemsOnStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(PUSH, 100, PTR_DIFF, HALT)
            cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testDerefMemoryNotInitialized() {
        val cpu = CPU(PUSH, 0, DEREF, HALT)
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 4)
        assertStackContains(cpu, 0) // Memory location 0 defaults to 0
    }

    @Test
    fun testDerefAfterWrite() {
        val cpu = CPU(
            PUSH, 42,    // value to write
            WRITE, 5,    // write to memory address 5
            PUSH, 5,     // pointer to address 5
            DEREF,       // dereference pointer
            HALT
        )
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 8)
        assertStackContains(cpu, 42) // Should read back the value 42
    }

    @Test
    fun testDerefNeedsOneItemOnStack() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(DEREF, HALT)
            cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testAddrOf() {
        val cpu = CPU(ADDR_OF, 3, HALT) // Get address of variable 3
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 3)
        assertStackContains(cpu, 3) // Simplified implementation returns variable number
    }

    @Test
    fun testAddrOfNeedsOneArgument() {
        assertFailsWith(InvalidProgramException::class) {
            val cpu = CPU(ADDR_OF)
            cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
            cpu.run()
        }
    }

    @Test
    fun testPointerArithmeticChain() {
        // Test: ptr = &var; ptr += 5; value = *ptr
        val cpu = CPU(
            ADDR_OF, 10,  // Get address of variable 10
            PUSH, 5,      // Offset
            PTR_ADD,      // Add offset to pointer
            DEREF,        // Dereference the result
            HALT
        )
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 7)
        assertStackContains(cpu, 0) // Memory at calculated address defaults to 0
    }

    @Test
    fun testComplexPointerOperations() {
        // Test pointer difference calculation
        val cpu = CPU(
            ADDR_OF, 20,  // Get address of variable 20
            ADDR_OF, 10,  // Get address of variable 10
            PTR_DIFF,     // Calculate difference
            HALT
        )
        cpu.appendDecoder(PointerDecoder(cpu, cpu.getStack()))
        assertProgramRunsToHaltAndInstructionAddressIs(cpu, 6)
        assertStackContains(cpu, 10) // 20 - 10 = 10
    }
}

