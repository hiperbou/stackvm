package com.hiperbou.vm.bytecode

val MAGIC = byteArrayOf(0x56, 0x4D, 0x42, 0x43)
const val VERSION = 0x01.toByte()

class InvalidBytecodeFileException(message: String) : Exception(message)
