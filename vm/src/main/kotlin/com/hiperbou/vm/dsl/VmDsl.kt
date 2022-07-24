package com.hiperbou.vm.dsl

import com.hiperbou.vm.Instructions.ADD
import com.hiperbou.vm.Instructions.AND
import com.hiperbou.vm.Instructions.B_AND
import com.hiperbou.vm.Instructions.B_OR
import com.hiperbou.vm.Instructions.B_XOR
import com.hiperbou.vm.Instructions.DIV
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
import com.hiperbou.vm.Instructions.OR
import com.hiperbou.vm.Instructions.POP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.READ
import com.hiperbou.vm.Instructions.STORE
import com.hiperbou.vm.Instructions.SUB
import com.hiperbou.vm.Instructions.WRITE
import com.hiperbou.vm.plugin.print.PrintInstructions.DEBUG_PRINT
import com.hiperbou.vm.plugin.print.PrintInstructions.PRINT

@DslMarker
annotation class ProgramMarker

@ProgramMarker
class PROGRAM {
    private val program = mutableListOf<Int>()

    private var variableIndex = 0
    private val variables = mutableMapOf<String, Int>()

    private fun write(value: Int) {
        program.add(value)
    }

    private fun writeDummy():Int {
        program.add(NOP)
        return program.lastIndex
    }

    fun write(vararg values: Int) {
        program.addAll(values.toTypedArray())
    }

    fun overwriteWithCurrentSize(index:Int) {
        program[index] = program.size
    }

    fun build():IntArray  {
        write(HALT)
        return program.toIntArray()
    }

    fun store(variable:String, value:Int) {
        write(PUSH, value, STORE, getVariable(variable))
    }

    fun store(variable:String, variable2:String) {
        write(LOAD, getVariable(variable2), STORE, getVariable(variable))
    }

    fun variable(value:Int):VmVariable {
        write(PUSH, value)
        return newVariableFromStack()
    }

    private fun newVariableFromStack():VmVariable {
        val variableIndex = newVariable()
        write(STORE, variableIndex)
        return VmVariable(variableIndex)
    }

    fun writeMemory(address:Int, value:Int) {
        write(PUSH, value, WRITE, address)
    }

    fun readMemory(address:Int) {
        write(READ, address)
    }

    private fun binaryExpression(a:Int, b:Int, opcode:Int) {
        write(PUSH, a, PUSH, b, opcode)
    }

    fun add(a:Int, b:Int) = binaryExpression(a, b, ADD)
    fun sub(a:Int, b:Int) = binaryExpression(a, b, SUB)
    fun mul(a:Int, b:Int) = binaryExpression(a, b, MUL)
    fun div(a:Int, b:Int) = binaryExpression(a, b, DIV)
    fun mod(a:Int, b:Int) = binaryExpression(a, b, MOD)
    fun min(a:Int, b:Int) = binaryExpression(a, b, MIN)
    fun max(a:Int, b:Int) = binaryExpression(a, b, MAX)
    fun and(a:Int, b:Int) = binaryExpression(a, b, AND)
    fun or(a:Int, b:Int)  = binaryExpression(a, b, OR)
    fun bAnd(a:Int, b:Int)  = binaryExpression(a, b, B_AND)
    fun bOr(a:Int, b:Int)  = binaryExpression(a, b, B_OR)
    fun bXor(a:Int, b:Int)  = binaryExpression(a, b, B_XOR)
    fun equals(a:Int, b:Int) = binaryExpression(a, b, EQ)
    fun notEquals(a:Int, b:Int) = binaryExpression(a, b, NE)
    fun greaterOrEquals(a:Int, b:Int) = binaryExpression(a, b, GTE)
    fun lessOrEquals(a:Int, b:Int) = binaryExpression(a, b, LTE)
    fun greaterThan(a:Int, b:Int) = binaryExpression(a, b, GT)
    fun lessThan(a:Int, b:Int) = binaryExpression(a, b, LT)

    private fun binaryVariableExpression(a:String, b:Int, opcode:Int) {
        write(LOAD, getVariable(a), PUSH, b, opcode)
    }

    fun add(a:String, b:Int) = binaryVariableExpression(a, b, ADD)
    fun sub(a:String, b:Int) = binaryVariableExpression(a, b, SUB)
    fun mul(a:String, b:Int) = binaryVariableExpression(a, b, MUL)
    fun div(a:String, b:Int) = binaryVariableExpression(a, b, DIV)
    fun mod(a:String, b:Int) = binaryVariableExpression(a, b, MOD)
    fun min(a:String, b:Int) = binaryVariableExpression(a, b, MIN)
    fun max(a:String, b:Int) = binaryVariableExpression(a, b, MAX)
    fun and(a:String, b:Int) = binaryVariableExpression(a, b, AND)
    fun or(a:String, b:Int)  = binaryVariableExpression(a, b, OR)
    fun bAnd(a:String, b:Int)  = binaryVariableExpression(a, b, B_AND)
    fun bOr(a:String, b:Int)  = binaryVariableExpression(a, b, B_OR)
    fun bXor(a:String, b:Int)  = binaryVariableExpression(a, b, B_XOR)
    fun equals(a:String, b:Int) = binaryVariableExpression(a, b, EQ)
    fun notEquals(a:String, b:Int) = binaryVariableExpression(a, b, NE)
    fun greaterOrEquals(a:String, b:Int) = binaryVariableExpression(a, b, GTE)
    fun lessOrEquals(a:String, b:Int) = binaryVariableExpression(a, b, LTE)
    fun greaterThan(a:String, b:Int) = binaryVariableExpression(a, b, GT)
    fun lessThan(a:String, b:Int) = binaryVariableExpression(a, b, LT)

    private fun binaryVariableExpression(a:Int, b:String, opcode:Int) {
        write(PUSH, a, LOAD, getVariable(b), opcode)
    }

    fun add(a:Int, b:String) = binaryVariableExpression(a, b, ADD)
    fun sub(a:Int, b:String) = binaryVariableExpression(a, b, SUB)
    fun mul(a:Int, b:String) = binaryVariableExpression(a, b, MUL)
    fun div(a:Int, b:String) = binaryVariableExpression(a, b, DIV)
    fun mod(a:Int, b:String) = binaryVariableExpression(a, b, MOD)
    fun min(a:Int, b:String) = binaryVariableExpression(a, b, MIN)
    fun max(a:Int, b:String) = binaryVariableExpression(a, b, MAX)
    fun and(a:Int, b:String) = binaryVariableExpression(a, b, AND)
    fun or(a:Int, b:String)  = binaryVariableExpression(a, b, OR)
    fun bAnd(a:Int, b:String)  = binaryVariableExpression(a, b, B_AND)
    fun bOr(a:Int, b:String)  = binaryVariableExpression(a, b, B_OR)
    fun bXor(a:Int, b:String)  = binaryVariableExpression(a, b, B_XOR)
    fun equals(a:Int, b:String) = binaryVariableExpression(a, b, EQ)
    fun notEquals(a:Int, b:String) = binaryVariableExpression(a, b, NE)
    fun greaterOrEquals(a:Int, b:String) = binaryVariableExpression(a, b, GTE)
    fun lessOrEquals(a:Int, b:String) = binaryVariableExpression(a, b, LTE)
    fun greaterThan(a:Int, b:String) = binaryVariableExpression(a, b, GT)
    fun lessThan(a:Int, b:String) = binaryVariableExpression(a, b, LT)

    private fun binaryVariableExpression(a:String, b:String, opcode:Int) {
        write(LOAD, getVariable(a), LOAD, getVariable(b), opcode)
    }

    fun add(a:String, b:String) = binaryVariableExpression(a, b, ADD)
    fun sub(a:String, b:String) = binaryVariableExpression(a, b, SUB)
    fun mul(a:String, b:String) = binaryVariableExpression(a, b, MUL)
    fun div(a:String, b:String) = binaryVariableExpression(a, b, DIV)
    fun mod(a:String, b:String) = binaryVariableExpression(a, b, MOD)
    fun min(a:String, b:String) = binaryVariableExpression(a, b, MIN)
    fun max(a:String, b:String) = binaryVariableExpression(a, b, MAX)
    fun and(a:String, b:String) = binaryVariableExpression(a, b, AND)
    fun or(a:String, b:String)  = binaryVariableExpression(a, b, OR)
    fun bAnd(a:String, b:String)  = binaryVariableExpression(a, b, B_AND)
    fun bOr(a:String, b:String)  = binaryVariableExpression(a, b, B_OR)
    fun bXor(a:String, b:String)  = binaryVariableExpression(a, b, B_XOR)
    fun equals(a:String, b:String) = binaryVariableExpression(a, b, EQ)
    fun notEquals(a:String, b:String) = binaryVariableExpression(a, b, NE)
    fun greaterOrEquals(a:String, b:String) = binaryVariableExpression(a, b, GTE)
    fun lessOrEquals(a:String, b:String) = binaryVariableExpression(a, b, LTE)
    fun greaterThan(a:String, b:String) = binaryVariableExpression(a, b, GT)
    fun lessThan(a:String, b:String) = binaryVariableExpression(a, b, LT)

    private fun binaryVariableExpression(a:VmVariable, b:Int, opcode:Int) {
        write(LOAD, a.index, PUSH, b, opcode)
    }

    fun add(a:VmVariable, b:Int) = binaryVariableExpression(a, b, ADD)
    fun sub(a:VmVariable, b:Int) = binaryVariableExpression(a, b, SUB)
    fun mul(a:VmVariable, b:Int) = binaryVariableExpression(a, b, MUL)
    fun div(a:VmVariable, b:Int) = binaryVariableExpression(a, b, DIV)
    fun mod(a:VmVariable, b:Int) = binaryVariableExpression(a, b, MOD)
    fun min(a:VmVariable, b:Int) = binaryVariableExpression(a, b, MIN)
    fun max(a:VmVariable, b:Int) = binaryVariableExpression(a, b, MAX)
    fun and(a:VmVariable, b:Int) = binaryVariableExpression(a, b, AND)
    fun or(a:VmVariable, b:Int)  = binaryVariableExpression(a, b, OR)
    fun bAnd(a:VmVariable, b:Int)  = binaryVariableExpression(a, b, B_AND)
    fun bOr(a:VmVariable, b:Int)  = binaryVariableExpression(a, b, B_OR)
    fun bXor(a:VmVariable, b:Int)  = binaryVariableExpression(a, b, B_XOR)
    fun equals(a:VmVariable, b:Int) = binaryVariableExpression(a, b, EQ)
    fun notEquals(a:VmVariable, b:Int) = binaryVariableExpression(a, b, NE)
    fun greaterOrEquals(a:VmVariable, b:Int) = binaryVariableExpression(a, b, GTE)
    fun lessOrEquals(a:VmVariable, b:Int) = binaryVariableExpression(a, b, LTE)
    fun greaterThan(a:VmVariable, b:Int) = binaryVariableExpression(a, b, GT)
    fun lessThan(a:VmVariable, b:Int) = binaryVariableExpression(a, b, LT)

    private fun binaryVariableExpression(a:Int, b:VmVariable, opcode:Int) {
        write(PUSH, a, LOAD, b.index, opcode)
    }

    fun add(a:Int, b:VmVariable) = binaryVariableExpression(a, b, ADD)
    fun sub(a:Int, b:VmVariable) = binaryVariableExpression(a, b, SUB)
    fun mul(a:Int, b:VmVariable) = binaryVariableExpression(a, b, MUL)
    fun div(a:Int, b:VmVariable) = binaryVariableExpression(a, b, DIV)
    fun mod(a:Int, b:VmVariable) = binaryVariableExpression(a, b, MOD)
    fun min(a:Int, b:VmVariable) = binaryVariableExpression(a, b, MIN)
    fun max(a:Int, b:VmVariable) = binaryVariableExpression(a, b, MAX)
    fun and(a:Int, b:VmVariable) = binaryVariableExpression(a, b, AND)
    fun or(a:Int, b:VmVariable)  = binaryVariableExpression(a, b, OR)
    fun bAnd(a:Int, b:VmVariable)  = binaryVariableExpression(a, b, B_AND)
    fun bOr(a:Int, b:VmVariable)  = binaryVariableExpression(a, b, B_OR)
    fun bXor(a:Int, b:VmVariable)  = binaryVariableExpression(a, b, B_XOR)
    fun equals(a:Int, b:VmVariable) = binaryVariableExpression(a, b, EQ)
    fun notEquals(a:Int, b:VmVariable) = binaryVariableExpression(a, b, NE)
    fun greaterOrEquals(a:Int, b:VmVariable) = binaryVariableExpression(a, b, GTE)
    fun lessOrEquals(a:Int, b:VmVariable) = binaryVariableExpression(a, b, LTE)
    fun greaterThan(a:Int, b:VmVariable) = binaryVariableExpression(a, b, GT)
    fun lessThan(a:Int, b:VmVariable) = binaryVariableExpression(a, b, LT)

    private fun binaryVariableExpression(a:VmVariable, b:VmVariable, opcode:Int) {
        write(LOAD, a.index, LOAD, b.index, opcode)
    }

    fun add(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, ADD)
    fun sub(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, SUB)
    fun mul(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, MUL)
    fun div(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, DIV)
    fun mod(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, MOD)
    fun min(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, MIN)
    fun max(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, MAX)
    fun and(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, AND)
    fun or(a:VmVariable, b:VmVariable)  = binaryVariableExpression(a, b, OR)
    fun bAnd(a:VmVariable, b:VmVariable)  = binaryVariableExpression(a, b, B_AND)
    fun bOr(a:VmVariable, b:VmVariable)  = binaryVariableExpression(a, b, B_OR)
    fun bXor(a:VmVariable, b:VmVariable)  = binaryVariableExpression(a, b, B_XOR)
    fun equals(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, EQ)
    fun notEquals(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, NE)
    fun greaterOrEquals(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, GTE)
    fun lessOrEquals(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, LTE)
    fun greaterThan(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, GT)
    fun lessThan(a:VmVariable, b:VmVariable) = binaryVariableExpression(a, b, LT)

    fun push(value:Int){
        write(PUSH, value)
    }

    fun load(variable:String) = getVariable(variable).apply {
        write(LOAD, this)
    }

    fun load(variable:VmVariable)  {
        write(LOAD, variable.index)
    }

    private fun getVariable(variable:String):Int {
        return variables.getOrPut(variable) { variableIndex++ }
    }

    private fun newVariable():Int {
        val id = "v_${variableIndex}"
        return getVariable(id)
    }

    fun debug(value:Int){
        write(PUSH, value, DEBUG_PRINT, POP)
    }
    fun print() = write(PRINT)

    fun repeatLoop(times:Int, block: PROGRAM.() -> Unit) {
        var i = variable(0)
        val loopStart = program.size
        //condition(i)
        ifLessThan(i, times) {
            block()
            i++
            write(JMP)
            write(loopStart)
        }
    }

    fun repeatLoop(times:VmVariable, block: PROGRAM.() -> Unit) {
        var i = variable(0)
        val loopStart = program.size

        ifLessThan(i, times) {
            block()
            i++
            write(JMP)
            write(loopStart)
        }
    }

    fun repeatLoopIndexed(times:Int, block: PROGRAM.(VmVariable) -> Unit) {
        var i = variable(0)
        val loopStart = program.size
        //condition(i)
        ifLessThan(i, times) {
            block(i)
            i++
            write(JMP)
            write(loopStart)
        }
    }

    fun repeatLoopIndexed(times:VmVariable, block: PROGRAM.(VmVariable) -> Unit) {
        var i = variable(0)
        val loopStart = program.size
        //condition(i)
        ifLessThan(i, times) {
            block(i)
            i++
            write(JMP)
            write(loopStart)
        }
    }

    fun ifElseCondition(condition: PROGRAM.() -> Unit,
                        ifBranch: PROGRAM.() -> Unit,
                        elseBranch: PROGRAM.() -> Unit = {}
    ) {
        condition()
        write(JIF)
        val jumpIfIndex = writeDummy()
        elseBranch()
        write(JMP)
        val jumpEndIndex = writeDummy()
        program[jumpIfIndex] = program.size
        ifBranch()
        program[jumpEndIndex] = program.size
    }

    fun ifCondition(condition: PROGRAM.() -> Unit,
                    ifBranch: PROGRAM.() -> Unit,
                    elseBranch: PROGRAM.() -> Unit = {}
    ) = ifElseCondition(condition, ifBranch, elseBranch)

    fun ifEquals            (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: Int, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)

    fun ifEquals            (a: String, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: String, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: String, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: String, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: String, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: String, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: String, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: String, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: String, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: String, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: String, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: String, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)

    fun ifEquals            (a: Int, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: Int, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: Int, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: Int, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: Int, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: Int, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: Int, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: Int, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: Int, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: Int, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: Int, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: Int, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)

    fun ifEquals            (a: String, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: String, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: String, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: String, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: String, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: String, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: String, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: String, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: String, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: String, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: String, b: String, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: String, b: String, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)

    fun ifEquals            (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: VmVariable, b: Int, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)

    fun ifEquals            (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: Int, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)

    fun ifEquals            (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { equals(a, b) }, ifBranch)
    fun ifEquals            (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { equals(a, b) }, ifBranch, elseBranch)
    fun ifNotEquals         (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { notEquals(a, b) }, ifBranch)
    fun ifNotEquals         (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { notEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterOrEquals   (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch)
    fun ifGreaterOrEquals   (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit) = ifCondition( { greaterOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifLessOrEquals      (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessOrEquals(a, b) }, ifBranch)
    fun ifLessOrEquals      (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessOrEquals(a, b) }, ifBranch, elseBranch)
    fun ifGreaterThan       (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { greaterThan(a, b) }, ifBranch)
    fun ifGreaterThan       (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { greaterThan(a, b) }, ifBranch, elseBranch)
    fun ifLessThan          (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit) = ifCondition( { lessThan(a, b) }, ifBranch)
    fun ifLessThan          (a: VmVariable, b: VmVariable, ifBranch: PROGRAM.() -> Unit, elseBranch: PROGRAM.() -> Unit = {}) = ifCondition( { lessThan(a, b) }, ifBranch, elseBranch)


    @ProgramMarker
    inner class VmVariable(val index:Int) {
        operator fun inc(): VmVariable {
            this@PROGRAM.add(this, 1)
            store()
            return this
        }

        private fun store() {
            this@PROGRAM.write(STORE, index)
        }

        operator fun dec(): VmVariable {
            this@PROGRAM.sub(this, 1)
            store()
            return this
        }

        operator fun plus(other:Int):VmVariable {
            this@PROGRAM.add(this, other)
            return this@PROGRAM.newVariableFromStack()
        }

        operator fun plus(other:VmVariable):VmVariable {
            this@PROGRAM.add(this, other)
            return this@PROGRAM.newVariableFromStack()
        }

        operator fun plusAssign(other:Int) {
            this@PROGRAM.add(this, other)
            store()
        }

        operator fun plusAssign(other:VmVariable) {
            this@PROGRAM.add(this, other)
            store()
        }

        fun add(other:Int) {
            this@PROGRAM.add(this, other)
            store()
        }

        fun add(other:VmVariable) {
            this@PROGRAM.add(this, other)
            store()
        }

        operator fun times(other:Int):VmVariable {
            this@PROGRAM.mul(this, other)
            return this@PROGRAM.newVariableFromStack()
        }

        operator fun times(other:VmVariable):VmVariable {
            this@PROGRAM.mul(this, other)
            return this@PROGRAM.newVariableFromStack()
        }

    }

    operator fun Int.times(other:VmVariable):VmVariable {
        mul(this, other)
        return newVariableFromStack()
    }
}

fun program(init: PROGRAM.() -> Unit): PROGRAM {
    return PROGRAM().apply { init() }
}