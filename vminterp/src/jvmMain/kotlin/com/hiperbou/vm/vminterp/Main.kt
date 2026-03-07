package com.hiperbou.vm.vminterp

fun main(args: Array<String>) {
    val options = CliParser().parse(args)
    InterpTool().run(options)
}
