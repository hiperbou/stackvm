package com.hiperbou.vm.asmcompiler

class CliParser {

    fun parse(args: Array<String>): ToolOptions {
        if (args.isEmpty()) {
            printHelp()
            kotlin.system.exitProcess(1)
        }

        var inputFile: String? = null
        var outputFile: String? = null
        var run = false // will be set to true as default below if no flags given
        var disassemble = false

        // Track if user explicitly passed any action flags
        var hasRunFlag = false
        var hasDisassembleFlag = false

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
                "--output", "-o" -> {
                    i++
                    if (i >= args.size) {
                        System.err.println("Error: '--output' / '-o' requires a file argument")
                        kotlin.system.exitProcess(1)
                    }
                    outputFile = args[i]
                }
                "--run", "-r" -> {
                    hasRunFlag = true
                    run = true
                }
                "--disassemble", "-d" -> {
                    hasDisassembleFlag = true
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

        // Default behaviour when no action flags are given: run = true
        if (!hasRunFlag && !hasDisassembleFlag) {
            run = true
        }

        return ToolOptions(
            inputFile = inputFile,
            outputFile = outputFile,
            run = run,
            disassemble = disassemble
        )
    }

    fun printHelp() {
        println("""
            |Usage: asmcompiler [options] <input-file>
            |
            |Options:
            |  -o, --output <file>   Write compiled bytecode to <file>
            |  -r, --run             Execute the compiled program (default when no flags given)
            |  -d, --disassemble     Disassemble and print the compiled program
            |  -h, --help            Show this help message and exit
            |  -v, --version         Show version information and exit
        """.trimMargin())
    }

    fun printVersion() {
        println("asmcompiler 1.0")
    }
}
