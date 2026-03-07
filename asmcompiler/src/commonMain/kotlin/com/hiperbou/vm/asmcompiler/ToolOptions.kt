package com.hiperbou.vm.asmcompiler

data class ToolOptions(
    val inputFile: String,
    val outputFile: String?,
    val run: Boolean,
    val disassemble: Boolean
)
