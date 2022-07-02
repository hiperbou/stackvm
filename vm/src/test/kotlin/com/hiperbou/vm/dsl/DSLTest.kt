package com.hiperbou.vm.dsl

import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.GTE
import com.hiperbou.vm.Instructions.LTE
import com.hiperbou.vm.Instructions.JIF
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.LOAD
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.assertProgramRunsToHaltAndInstructionAddressIs
import com.hiperbou.vm.assertStackContains
import com.hiperbou.vm.plugin.print.PrintDecoder
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test


class DSLTest {
    @Test
    fun storeVariableTest() {
        val expected = intArrayOf(
            PUSH, 3,
            STORE, 0,
            HALT
        )

        val program = program {
            store("x", 3)
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program)
    }

    @Test
    fun storeVariablesTest() {
        val expected = intArrayOf(
            PUSH, 3,
            STORE, 0,
            PUSH, 4,
            STORE, 1,
            HALT
        )

        val program = program {
            store("x", 3)
            store("y", 4)
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program)
    }

    @Test
    fun storeSetVariablesTest() {
        val expected = intArrayOf(
            PUSH, 3,
            STORE, 0,
            LOAD, 0,
            STORE, 1,
            HALT
        )

        val program = program {
            store("x", 3)
            store("y", "x")
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program)
    }

    @Test
    fun greaterOrEqualsTest() {
        val expected = intArrayOf(
            PUSH, 1,
            PUSH, 0,
            GTE,
            HALT
        )

        val program = program {
            greaterOrEquals(1,0)
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 1)
    }

    @Test
    fun lessOrEqualsTest() {
        val expected = intArrayOf(
            PUSH, 1,
            PUSH, 0,
            LTE,
            HALT
        )

        val program = program {
            lessOrEquals(1,0)
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 0)
    }

    @Test
    fun conditionIfTest() {
        val expected = intArrayOf(
            PUSH, 1,
            PUSH, 0,
            GTE,
            JIF, 9,
            JMP, 11,
            PUSH, 0,
            HALT
        )

        val program = program {
            ifCondition({
                greaterOrEquals(1,0)
            },{
                load(0)
            })
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 0)
    }

    @Test
    fun conditionIfTest2() {
        val program = program {
            load(0)
            ifCondition({
                greaterOrEquals(1,0)
            },{
                load(1)
                load(2)
                load(3)
            })
            load(4)
        }.build()

        assertProgramFinishes(program, 4, 3, 2, 1, 0)
    }

    @Test
    fun conditionIfElseTest() {
        val expected = intArrayOf(
            PUSH, 1,
            PUSH, 0,
            GTE,
            JIF,
            11,
            PUSH, 0x29A,
            JMP, 13,
            PUSH, 0,
            HALT
        )

        val program = program {
            ifElseCondition({
                greaterOrEquals(1,0)
            },{
                load(0)
            }, {
                load(0x29A)
            })
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 0)
    }

    @Test
    fun conditionIfElseTest2() {
        val expected = intArrayOf(
            PUSH, 0,
            PUSH, 1,
            GTE,
            JIF,
            11,
            PUSH, 0x29A,
            JMP, 13,
            PUSH, 0,
            HALT
        )

        val program = program {
            ifElseCondition({
                greaterOrEquals(0,1)
            },{
                load(0)
            }, {
                load(0x29A)
            })
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 0x29A)
    }

    @Test
    fun conditionIfElseTest3() {
        val expected = intArrayOf(
            PUSH, 1,
            PUSH, 0,
            LTE,
            JIF,
            11,
            PUSH, 0x29A,
            JMP, 13,
            PUSH, 0,
            HALT
        )

        val program = program {
            ifElseCondition({
                lessOrEquals(1,0)
            },{
                load(0)
            }, {
                load(0x29A)
            })
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 0x29A)
    }

    @Test
    fun conditionIfElseWithVariablesTest() {
        val expected = intArrayOf(
            PUSH, 0,
            STORE, 0,
            PUSH, 1,
            PUSH, 0,
            GTE,
            JIF, 21,
            PUSH, 2,
            STORE, 0,
            PUSH, 22,
            STORE, 1,
            JMP, 29,
            PUSH, 1,
            STORE, 0,
            PUSH, 11,
            STORE, 1,
            LOAD, 0,
            LOAD, 1,
            HALT
        )

        val program = program {
            store("x", 0)
            ifElseCondition({
                greaterOrEquals(1,0)
            },{
                store("x", 1)
                store("y", 11)
            }, {
                store("x", 2)
                store("y", 22)
            })
            load("x")
            load("y")
        }.build()

        assertArrayEquals(expected, program)
        assertProgramFinishes(program, 11, 1)
    }

    @Test
    fun nestedConditionIfElseTest() {
        val program = program {
            store("x", 0)
            ifElseCondition({
                greaterOrEquals(1,0)
            },{
                ifElseCondition({
                    lessOrEquals(1,0)
                },{
                    store("x", 1)
                }, {
                    store("x", 2)
                })
            }, {
                store("x", 3)
            })
            load("x")
        }.build()

        assertProgramFinishes(program, 2)
    }

    @Test
    fun niceConditionIfTest() {
        val expected = intArrayOf(
            PUSH, 1,
            PUSH, 0,
            GTE,
            JIF, 9,
            JMP, 11,
            PUSH, 0,
            HALT
        )

        val program = program {
            ifGreaterOrEquals(1,0) {
                load(0)
            }
        }.build()

        assertArrayEquals(expected, program)

        assertProgramFinishes(program, 0)
    }

    @Test
    fun allIfConditionsTest() {
        assertProgramFinishes(program {
            ifEquals(1,1) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifNotEquals(1,1) {
                load(0)
            }
        }.build())
        assertProgramFinishes(program {
            ifGreaterOrEquals(1,0) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifLessOrEquals(1,0) {
                load(0)
            }
        }.build())
        assertProgramFinishes(program {
            ifGreaterOrEquals(1,1) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifLessOrEquals(1,1) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifGreaterThan(1,0) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifLessThan(1,0) {
                load(0)
            }
        }.build())
    }

    @Test
    fun allIfElseConditionsTest() {
        assertProgramFinishes(program {
            ifEquals(1, 1, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 1)
        assertProgramFinishes(program {
            ifNotEquals(1, 1, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifGreaterOrEquals(1, 0, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 1)
        assertProgramFinishes(program {
            ifGreaterOrEquals(1, 1, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 1)
        assertProgramFinishes(program {
            ifLessOrEquals(1, 0, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifLessOrEquals(1, 1, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 1)
        //---
        assertProgramFinishes(program {
            ifGreaterThan(1, 0, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 1)
        assertProgramFinishes(program {
            ifGreaterThan(1, 1, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifLessThan(1, 0, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 0)
        assertProgramFinishes(program {
            ifLessThan(1, 1, {
                load(1)
            }) {
                load(0)
            }
        }.build(), 0)
    }

    @Test
    fun variablesUsageTest() {
        assertProgramFinishes(program {
            store("x", 320)
            ifEquals("x",320) {
                load(1)
            }
        }.build(), 1)

        assertProgramFinishes(program {
            store("x", 320)
            ifEquals(320, "x") {
                load(1)
            }
        }.build(), 1)

        assertProgramFinishes(program {
            store("x", 320)
            store("y", 320)
            ifEquals("y", "x") {
                load(1)
            }
        }.build(), 1)
    }

    @Test
    fun variableDSLTest() {
        assertProgramFinishes(program {
            val x = variable(320)
            ifEquals(x,320) {
                load(1)
            }
        }.build(), 1)

        assertProgramFinishes(program {
            val x = variable(320)
            ifEquals(320, x) {
                load(1)
            }
        }.build(), 1)

        assertProgramFinishes(program {
            val x = variable(320)
            val y = variable(320)
            ifEquals(x, y) {
                load(1)
            }
        }.build(), 1)
    }

    @Test
    fun variableDSLIncrementTest() {
        assertProgramFinishes(program {
            var x = variable(0)
            x++
            load(x)
        }.build(), 1)

        assertProgramFinishes(program {
            var x = variable(0)
            x++
            x++
            load(x)
        }.build(), 2)
    }

    @Test
    fun variableDSLDecrementTest() {
        assertProgramFinishes(program {
            var x = variable(0)
            x--
            load(x)
        }.build(), -1)

        assertProgramFinishes(program {
            var x = variable(0)
            x--
            x--
            load(x)
        }.build(), -2)
    }

    @Test
    fun variableDSLAssignmentTest() {
        assertProgramFinishes(program {
            val x = variable(0)
            x += 1
            load(x)
        }.build(), 1)

        assertProgramFinishes(program {
            val x = variable(0)
            x += 1
            x += 1
            load(x)
        }.build(), 2)

        assertProgramFinishes(program {
            val x = variable(1)
            x += 3
            load(x)
        }.build(), 4)
    }

    @Test
    fun variableDSLAdditionTest() {
        assertProgramFinishes(program {
            val x = variable(1)
            val y = variable(2)
            val z = variable(3)
            load(x + y + z + 10)
        }.build(), 16)
    }

    @Test
    fun variableDSLMultiplyTest() {
        assertProgramFinishes(program {
            val x = variable(1)
            val y = variable(2)
            val z = variable(3)
            load(x * y )
            load(x * y * z)
            val zero = variable(0)
            load(zero * z)
            load(zero * x * y * z)
        }.build(),   0, 0, 6, 2)
    }

    @Test
    fun variableDSLMultiplyPrecedenceTest() {
        assertProgramFinishes(program {
            val x = variable(3)
            val y = variable(4)
            val z = variable(5)
            load(x + y * z)
            load(y * z + x)
            load(z * y + x)
            load((x + y) * z)
        }.build(),  35, 23, 23, 23)
    }

    @Test
    fun repeatTest() {
        val program = program {
            val result = variable(0)
            //Multiply 3 * 4 using a loop
            repeatLoop(4) {
                result += 3
            }
            load(result)
        }.build()

        assertProgramFinishes(program, 12)
    }

    @Test
    fun repeatVariableTest() {
        val program = program {
            val result = variable(0)
            val times = variable(4)
            //Multiply 3 * 4 using a loop
            repeatLoop(times) {
                result += 3
            }
            load(result)
        }.build()

        assertProgramFinishes(program, 12)
    }

    @Test
    fun repeatIndexedTest() {
        val program = program {
            val result = variable(0)
            repeatLoopIndexed(4) { index ->
                result += 3 * index
            }
            load(result)
        }.build()

        assertProgramFinishes(program, 18)
    }

    @Test
    fun repeatIndexedVariableTest() {
        val program = program {
            val result = variable(0)
            val times = variable(4)
            repeatLoopIndexed(times) { index ->
                result += 3 * index
            }
            load(result)
        }.build()

        assertProgramFinishes(program, 18)
    }

    @Test
    fun repeatIndexedConditionTest() {
        val program = program {
            val result = variable(0)
            var result2 = variable(0)
            repeatLoopIndexed(10) { index ->
                result += 1
                ifGreaterOrEquals(index, 5) {
                    result2++
                }
            }
            load(result)
            load(result2)
        }.build()

        assertProgramFinishes(program, 5, 10)
    }

    private fun assertProgramFinishes(program:IntArray, vararg expectedContent: Int) {
        CPU(*program).apply {
            appendDecoder(PrintDecoder(getStack()))
            run()
            assertProgramRunsToHaltAndInstructionAddressIs(this, program.size)
            assertStackContains(this, *expectedContent)
        }
    }
}