package com.hiperbou.vm.vminterp

class CliParser {

    fun parse(args: Array<String>): InterpOptions {
        if (args.isEmpty()) {
            printHelp()
            kotlin.system.exitProcess(1)
        }

        var inputFile: String? = null
        var disassemble = false

        var i = 0
        while (i < args.size) {
            when (val arg = args[i]) {
                "--help", "-h" -> {
                    printHelp()
                    kotlin.system.exitProcess(0)
                }
                "--version", "-v" -> {
                    printVersion()
                    kotlin.system.exitProcess(0)
                }
                "--disassemble", "-d" -> {
                    disassemble = true
                }
                else -> {
                    if (arg.startsWith("-")) {
                        System.err.println("Error: Unknown option '$arg'")
                        printHelp()
                        kotlin.system.exitProcess(1)
                    }
                    if (inputFile != null) {
                        System.err.println("Error: Multiple input files specified")
                        kotlin.system.exitProcess(1)
                    }
                    inputFile = arg
                }
            }
            i++
        }

        if (inputFile == null) {
            System.err.println("Error: No input file specified")
            printHelp()
            kotlin.system.exitProcess(1)
        }

        return InterpOptions(
            inputFile = inputFile,
            disassemble = disassemble
        )
    }

    fun printHelp() {
        println("""
            |Usage: vminterp [options] <bytecode-file>
            |
            |Options:
            |  -d, --disassemble     Disassemble and print the bytecode (instead of running)
            |  -h, --help            Show this help message and exit
            |  -v, --version         Show version information and exit
        """.trimMargin())
    }

    fun printVersion() {
        println("vminterp 1.0")
    }
}
