# Full SDD workflow

## Configuration
- **Artifacts Path**: {@artifacts_path} → `.zenflow/tasks/{task_id}`

---

## Agent Instructions

If you are blocked and need user clarification, mark the current step with `[!]` in plan.md before stopping.

---

## Workflow Steps

### [x] Step: Requirements
<!-- chat-id: 3bc4da83-2aa2-4de3-82c9-069afb9b95c1 -->

Create a Product Requirements Document (PRD) based on the feature description.

1. Review existing codebase to understand current architecture and patterns
2. Analyze the feature definition and identify unclear aspects
3. Ask the user for clarifications on aspects that significantly impact scope or user experience
4. Make reasonable decisions for minor details based on context and conventions
5. If user can't clarify, make a decision, state the assumption, and continue

Save the PRD to `{@artifacts_path}/requirements.md`.

### [x] Step: Technical Specification
<!-- chat-id: 6f80cc62-ce48-4e54-ad90-5825dd8468ef -->

Create a technical specification based on the PRD in `{@artifacts_path}/requirements.md`.

1. Review existing codebase architecture and identify reusable components
2. Define the implementation approach

Save to `{@artifacts_path}/spec.md` with:
- Technical context (language, dependencies)
- Implementation approach referencing existing code patterns
- Source code structure changes
- Data model / API / interface changes
- Delivery phases (incremental, testable milestones)
- Verification approach using project lint/test commands

### [x] Step: Planning
<!-- chat-id: e0bf3bb4-2527-4ac4-9f7b-2faa09cdb762 -->

Create a detailed implementation plan based on `{@artifacts_path}/spec.md`.

### [x] Step: Add bytecode format utility to `vm` module
<!-- chat-id: b05ef355-52c0-4746-be55-95d1e45474ce -->

Add shared binary serialization for the `.vmb` format (spec §3, §4.1).

- Create `vm/src/commonMain/kotlin/com/hiperbou/vm/bytecode/BytecodeFormat.kt`
  - Define magic constant `MAGIC = byteArrayOf(0x56, 0x4D, 0x42, 0x43)`, `VERSION = 0x01.toByte()`
  - Define `InvalidBytecodeFileException : Exception`
- Create `vm/src/commonMain/kotlin/com/hiperbou/vm/bytecode/BytecodeWriter.kt`
  - `fun write(bytecode: IntArray): ByteArray` — writes header + instructions as big-endian
- Create `vm/src/commonMain/kotlin/com/hiperbou/vm/bytecode/BytecodeReader.kt`
  - `fun read(data: ByteArray): IntArray` — validates magic/version, reads instructions; throws `InvalidBytecodeFileException` on bad input
- Create `vm/src/jvmMain/kotlin/com/hiperbou/vm/bytecode/BytecodeFile.kt`
  - `fun writeTo(file: java.io.File, bytecode: IntArray)` — calls `BytecodeWriter.write()` then `file.writeBytes()`
  - `fun readFrom(file: java.io.File): IntArray` — calls `BytecodeReader.read(file.readBytes())`
- Add unit tests in `vm/src/commonTest/kotlin/com/hiperbou/vm/bytecode/BytecodeFormatTest.kt`
  - Round-trip: write then read returns same `IntArray`
  - Bad magic bytes → `InvalidBytecodeFileException`
  - Bad version byte → `InvalidBytecodeFileException`
- Verify: `./gradlew :vm:build` passes

### [ ] Step: Create `asmcompiler` standalone module
<!-- chat-id: f725cfa4-be34-4888-b7dd-cdc869524b4f -->

New KMP module for the assembly language compiler CLI producing `asmcompiler.jar` (spec §4.2, §5.2).

- Add Shadow plugin to `settings.gradle` `pluginManagement` plugins block:
  `id 'com.github.johnrengelman.shadow' version '7.1.2'`
- Add `'asmcompiler'` to `include` line in root `settings.gradle`
- Create `asmcompiler/build.gradle` (KMP + Shadow, depends on `:vm`, `Main-Class = 'com.hiperbou.vm.asmcompiler.MainKt'`, archive base name `asmcompiler`)
- Create `asmcompiler/src/commonMain/kotlin/com/hiperbou/vm/asmcompiler/ToolOptions.kt`
  - `data class ToolOptions(inputFile: String, outputFile: String?, run: Boolean, disassemble: Boolean)`
- Create `asmcompiler/src/jvmMain/kotlin/com/hiperbou/vm/asmcompiler/CliParser.kt`
  - Parses `args: Array<String>` into `ToolOptions`; handles `--output`/`-o`, `--run`/`-r`, `--disassemble`/`-d`, `--help`/`-h`, `--version`/`-v`
  - Default (no flags): `run = true`, `outputFile = null`, `disassemble = false`
  - `printHelp()` and `printVersion()` helpers
- Create `asmcompiler/src/jvmMain/kotlin/com/hiperbou/vm/asmcompiler/AsmTool.kt`
  - `run(options: ToolOptions)` orchestrates: read file → `Compiler().generateProgram()` → optional `BytecodeFile.writeTo()` → optional disassemble (`ProgramDecompiler` + `Disassembler`) → optional `CPU` execute (with `PrintDecoder` attached, `OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation())`)
  - Catches exceptions, prints to `stderr`, calls `exitProcess(1)`
- Create `asmcompiler/src/jvmMain/kotlin/com/hiperbou/vm/asmcompiler/Main.kt`
  - `fun main(args: Array<String>)` — delegates to `CliParser` then `AsmTool`
- Verify: `./gradlew :asmcompiler:shadowJar` succeeds and produces `asmcompiler/build/libs/asmcompiler.jar`

### [ ] Step: Add `jvmMain` entry point to `ccompilerbin`

Add JVM CLI (`ccompiler.jar`) to the existing `ccompilerbin` KMP module (spec §4.3, §5.3).

- Add `id 'com.github.johnrengelman.shadow'` to `ccompilerbin/build.gradle` plugins block
- Add `shadowJar` task to `ccompilerbin/build.gradle`:
  - `archiveBaseName = 'ccompiler'`, no classifier/version, `jvmRuntimeClasspath`, `Main-Class = 'com.hiperbou.vm.ccompilerbin.MainKt'`
- Create `ccompilerbin/src/jvmMain/kotlin/com/hiperbou/vm/ccompilerbin/CliParser.kt`
  - Same structure as `asmcompiler`'s `CliParser` but for `ccompilerbin` package
- Create `ccompilerbin/src/jvmMain/kotlin/com/hiperbou/vm/ccompilerbin/CCompilerTool.kt`
  - Same orchestration as `AsmTool` but uses `CCompiler().compile()` instead of `Compiler().generateProgram()`
- Create `ccompilerbin/src/jvmMain/kotlin/com/hiperbou/vm/ccompilerbin/Main.kt`
  - `fun main(args: Array<String>)` — delegates to `CliParser` then `CCompilerTool`
- Verify: `./gradlew :ccompilerbin:shadowJar` succeeds and produces `ccompilerbin/build/libs/ccompiler.jar`

### [ ] Step: Create `vminterp` standalone module

New KMP module for the bytecode interpreter CLI producing `vminterp.jar` (spec §4.4, §5.2).

- Add `'vminterp'` to `include` line in root `settings.gradle`
- Create `vminterp/build.gradle` (KMP + Shadow, depends on `:vm` only, `Main-Class = 'com.hiperbou.vm.vminterp.MainKt'`, archive base name `vminterp`)
- Create `vminterp/src/commonMain/kotlin/com/hiperbou/vm/vminterp/InterpOptions.kt`
  - `data class InterpOptions(inputFile: String, disassemble: Boolean)`
- Create `vminterp/src/jvmMain/kotlin/com/hiperbou/vm/vminterp/CliParser.kt`
  - Parses `args` into `InterpOptions`; handles `--disassemble`/`-d`, `--help`/`-h`, `--version`/`-v`
  - Default: execute the binary
- Create `vminterp/src/jvmMain/kotlin/com/hiperbou/vm/vminterp/InterpTool.kt`
  - `run(options: InterpOptions)` orchestrates: `BytecodeFile.readFrom()` → `BytecodeReader.read()` → disassemble or execute with `CPU` + `PrintDecoder`
  - Catches `InvalidBytecodeFileException` and other exceptions, prints to `stderr`, calls `exitProcess(1)`
- Create `vminterp/src/jvmMain/kotlin/com/hiperbou/vm/vminterp/Main.kt`
  - `fun main(args: Array<String>)` — delegates to `CliParser` then `InterpTool`
- Verify: `./gradlew :vminterp:shadowJar` succeeds and produces `vminterp/build/libs/vminterp.jar`

### [ ] Step: End-to-end build verification

Ensure all modules build and produce valid JARs.

- Run `./gradlew :vm:build` — all bytecode format unit tests pass
- Run `./gradlew :asmcompiler:shadowJar` — `asmcompiler.jar` produced
- Run `./gradlew :ccompilerbin:shadowJar` — `ccompiler.jar` produced
- Run `./gradlew :vminterp:shadowJar` — `vminterp.jar` produced
- Record results in this plan
