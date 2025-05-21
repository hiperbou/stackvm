package com.hiperbou.vm

object Instructions {
    const val NOP = 0x00
    const val HALT = 0x01
    const val PUSH = 0x02
    const val POP = 0x03
    const val DUP = 0x04

    const val ADD = 0x10
    const val SUB = 0x11
    const val MUL = 0x12
    const val DIV = 0x13
    const val MOD = 0x14
    const val MIN = 0x15
    const val MAX = 0x16

    const val NOT = 0x20
    const val B_NOT = 0x21
    const val ABS = 0x22
    const val NEG = 0x23

    const val AND = 0x30
    const val OR = 0x31
    const val B_AND = 0x32
    const val B_OR = 0x33
    const val B_XOR = 0x34

    const val EQ = 0x40
    const val NE = 0x41
    const val GTE = 0x42
    const val LTE = 0x43
    const val GT = 0x44
    const val LT = 0x45

    const val JMP = 0x50
    const val JIF = 0x51 //possible opcodes JMPT, JMPF
    //const val JMPT = 0x51
    //const val JMPF = 0x52

    const val LOAD = 0x60
    const val STORE = 0x61
    const val GLOAD = 0x62
    const val GSTORE = 0x63
    const val READ = 0x64
    const val WRITE = 0x65

    const val LOADI = 0x66
    const val STOREI = 0x67
    const val GLOADI = 0x68
    const val GSTOREI = 0x69
    const val READI = 0x70
    const val WRITEI = 0x71

    const val CALL = 0x72
    const val CALLI = 0x73
    const val RET = 0x74
}

interface Instruction
interface Opcode:Instruction
{
    val opcode:Int
    val params:Int
    val label:Boolean
}
enum class InstructionsEnum(override val opcode:Int, override val params:Int = 0, override val label:Boolean = false):Opcode {
    NOP(Instructions.NOP),
    HALT(Instructions.HALT),
    PUSH(Instructions.PUSH, 1),
    POP(Instructions.POP),
    DUP(Instructions.DUP),
    ADD(Instructions.ADD),
    SUB(Instructions.SUB),
    MUL(Instructions.MUL),
    DIV(Instructions.DIV),
    MOD(Instructions.MOD),
    MIN(Instructions.MIN),
    MAX(Instructions.MAX),
    NOT(Instructions.NOT),
    B_NOT(Instructions.B_NOT),
    ABS(Instructions.ABS),
    NEG(Instructions.NEG),
    AND(Instructions.AND),
    OR(Instructions.OR),
    B_AND(Instructions.B_AND),
    B_OR(Instructions.B_OR),
    B_XOR(Instructions.B_XOR),
    EQ(Instructions.EQ),
    NE(Instructions.NE),
    GTE(Instructions.GTE),
    LTE(Instructions.LTE),
    GT(Instructions.GT),
    LT(Instructions.LT),
    JMP(Instructions.JMP, 1, true),
    JIF(Instructions.JIF, 1, true),
    LOAD(Instructions.LOAD, 1),
    STORE(Instructions.STORE, 1),
    GLOAD(Instructions.GLOAD, 1),
    GSTORE(Instructions.GSTORE, 1),
    READ (Instructions.READ, 1),
    WRITE(Instructions.WRITE, 1),
    LOADI(Instructions.LOADI),
    STOREI(Instructions.STOREI),
    GLOADI(Instructions.GLOADI),
    GSTOREI(Instructions.GSTOREI),
    READI (Instructions.READI),
    WRITEI(Instructions.WRITEI),
    CALL(Instructions.CALL, 1, true),
    CALLI(Instructions.CALLI),
    RET(Instructions.RET);

    companion object{
        fun valueOfOrNull(value:String):InstructionsEnum? {
            return try {
                valueOf(value)
            } catch (e:Exception){
                null
            }
        }
    }
}