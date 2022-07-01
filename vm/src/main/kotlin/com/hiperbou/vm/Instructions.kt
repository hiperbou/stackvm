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
    const val JIF = 0x51

    const val LOAD = 0x60
    const val STORE = 0x61

    const val CALL = 0x70
    const val RET = 0x71
}