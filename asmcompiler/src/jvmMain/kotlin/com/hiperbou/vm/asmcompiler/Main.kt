package com.hiperbou.vm.asmcompiler

fun main(args: Array<String>) {
    val options = CliParser().parse(args)
    AsmTool().run(options)
}
