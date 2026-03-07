package com.hiperbou.vm.ccompilerbin

data class ToolOptions(
    val inputFile: String,
    val outputFile: String?,
    val run: Boolean,
    val disassemble: Boolean
)
