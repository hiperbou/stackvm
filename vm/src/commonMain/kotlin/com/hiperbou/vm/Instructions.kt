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
    NOP(0x00),
    HALT(0x01),
    PUSH(0x02, 1),
    POP(0x03),
    DUP(0x04),
    ADD(0x10),
    SUB(0x11),
    MUL(0x12),
    DIV(0x13),
    MOD(0x14),
    MIN(0x15),
    MAX(0x16),
    NOT(0x20),
    B_NOT(0x21),
    ABS(0x22),
    AND(0x30),
    OR(0x31),
    B_AND(0x32),
    B_OR(0x33),
    B_XOR(0x34),
    EQ(0x40),
    NE(0x41),
    GTE(0x42),
    LTE(0x43),
    GT(0x44),
    LT(0x45),
    JMP(0x50, 1, true),
    JIF(0x51, 1, true),
    LOAD(0x60, 1),
    STORE(0x61, 1),
    GLOAD(0x62, 1),
    GSTORE(0x63, 1),
    READ (0x64, 1),
    WRITE(0x65, 1),
    LOADI(0x66),
    STOREI(0x67),
    GLOADI(0x68),
    GSTOREI(0x69),
    READI (0x70),
    WRITEI(0x71),
    CALL(0x72, 1, true),
    CALLI(0x73),
    RET(0x74);

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