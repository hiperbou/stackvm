package com.hiperbou.vm.ccompilerbin

fun main(args: Array<String>) {
    val options = CliParser().parse(args)
    CCompilerTool().run(options)
}
