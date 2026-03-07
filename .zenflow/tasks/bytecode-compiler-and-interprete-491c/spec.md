# Technical Specification: Standalone Bytecode Compiler and Interpreter Toolchain

---

## 1. Technical Context

| Item | Value |
|------|-------|
| Language | Kotlin Multiplatform (JVM target active now; Native-ready structure) |
| Build tool | Gradle 7.4 |
| Kotlin version | 1.7.10 |
| Shadow JAR plugin | `com.github.johnrengelman.shadow` v7.1.2 (compatible with Gradle 7.x) |
| JVM target | 1.8 |
| Package root | `com.hiperbou.vm` |

### Key Existing APIs

| Class / Object | Module | Role |
|---|---|---|
| `Compiler` | `vm` | `generateProgram(String): IntArray` — asm text → bytecode |
| `CCompiler` | `ccompiler` | `compile(String): IntArray` — C-like text → bytecode |
| `CPU(IntArray)` | `vm` | Executes `IntArray` bytecode |
| `ProgramDecompiler` | `vm` | `decompile(IntArray): List<Instruction>` |
| `Disassembler` | `vm` | `disassemble(List<Instruction>): String` |
| `OpcodeInformationChain` | `vm` | Chains multiple `OpcodeInformation` providers |
| `CoreOpcodeInformation` | `vm` | Resolves base instruction set |
| `PrintOpcodeInformation` | `vm` | Resolves PRINT / DEBUG_PRINT opcodes (used by C compiler output) |
| `PrintDecoder` | `vm` | Runtime decoder for PRINT / DEBUG_PRINT |

---

## 2. Implementation Approach

### 2.1 Module Strategy

Three new / modified modules:

| Module | Type | Change |
|--------|------|--------|
| `vm` | KMP (existing) | Add `BytecodeWriter` / `BytecodeReader` in `commonMain`; JVM file I/O in `jvmMain` |
| `asmcompiler` | KMP (new) | Standalone asm compiler CLI + Shadow JAR; JVM active, Native-ready |
| `ccompilerbin` | KMP (existing) | Add `jvmMain` entry point (mirrors `vmbin` pattern) + Shadow JAR task |
| `vminterp` | KMP (new) | Standalone bytecode interpreter CLI + Shadow JAR; JVM active, Native-ready |

**Decision — `ccompilerbin` vs new module**: Add `jvmMain` directly to `ccompilerbin`, following the established pattern of `vmbin` (which hosts both the web editor in `jsMain` and the JVM CLI in `jvmMain`). This keeps the module count reasonable and stays consistent with the existing architecture.

**Decision — `asmcompiler` and `vminterp` as KMP modules**: Both use `org.jetbrains.kotlin.multiplatform` plugin with a `jvm()` target now and platform-specific CLI code in `jvmMain`. The `commonMain` holds only shared logic (none yet — CLI is inherently platform-specific). This structure allows adding a `linuxX64()` / `macosX64()` / `mingwX64()` Native target in the future without restructuring. Source layout follows the standard KMP convention (`src/commonMain/kotlin`, `src/jvmMain/kotlin`).

### 2.2 CLI Argument Parsing

No external CLI library is used (codebase has no precedent for such a dependency). A small hand-written argument parser class (`CliParser`) is shared within each tool's own module (no cross-module sharing of CLI utilities needed, as each JAR is self-contained).

### 2.3 CPU Setup for C-compiled Programs

C-like programs use `PRINT` / `DEBUG_PRINT` instructions (opcode `0xF0` / `0xF1`) via the plugin system. All tools that execute bytecode must attach a `PrintDecoder` and use `OpcodeInformationChain(CoreOpcodeInformation(), PrintOpcodeInformation())` for disassembly — matching the `WebEditor.kt` pattern in `ccompilerbin`.

The `asmcompiler` and `vminterp` tools also use this setup so that assembly programs using `PRINT` work correctly.

### 2.4 Default Behavior

Invoking any compiler tool with only an input file compiles and runs the program (no binary output), matching the `python script.py` / `node script.js` idiom.

---

## 3. Binary File Format

Handled by `BytecodeWriter` / `BytecodeReader` in `vm/commonMain`:

```
Offset   Size    Description
------   ----    -----------
0        4       Magic: 0x56 0x4D 0x42 0x43  ("VMBC")
4        1       Version: 0x01
5        4       Instruction count (big-endian Int)
9        N*4     Instructions (each a big-endian Int)
```

**Errors**: `BytecodeReader` throws `InvalidBytecodeFileException` (new, in `vm/commonMain`) on bad magic or unsupported version.

---

## 4. Source Code Structure Changes

### 4.1 `vm` module — new files

```
vm/src/commonMain/kotlin/com/hiperbou/vm/bytecode/
    BytecodeFormat.kt        # Constants: MAGIC, VERSION, InvalidBytecodeFileException
    BytecodeWriter.kt        # fun write(bytecode: IntArray): ByteArray
    BytecodeReader.kt        # fun read(data: ByteArray): IntArray

vm/src/jvmMain/kotlin/com/hiperbou/vm/bytecode/
    BytecodeFile.kt          # fun writeTo(file: java.io.File, bytecode: IntArray)
                             # fun readFrom(file: java.io.File): IntArray
```

`BytecodeWriter` and `BytecodeReader` use only Kotlin stdlib (`ByteArray`, `Int.and()`, bitshift operators) — no `java.io` in `commonMain`.

### 4.2 `asmcompiler` module — new

```
asmcompiler/
    build.gradle
    src/commonMain/kotlin/com/hiperbou/vm/asmcompiler/
        ToolOptions.kt       # data class ToolOptions(...) — shared across platforms
    src/jvmMain/kotlin/com/hiperbou/vm/asmcompiler/
        Main.kt              # fun main(args: Array<String>)
        CliParser.kt         # Parses args → ToolOptions; printHelp(); printVersion()
        AsmTool.kt           # Orchestrates compile / write / run / disassemble (uses java.io.File)
```

`ToolOptions` data class:
```kotlin
data class ToolOptions(
    val inputFile: String,
    val outputFile: String?,   // null = no binary output
    val run: Boolean,
    val disassemble: Boolean
)
```

Default when no flags: `run = true`, `outputFile = null`, `disassemble = false`.

### 4.3 `ccompilerbin` module — new `jvmMain`

```
ccompilerbin/src/jvmMain/kotlin/com/hiperbou/vm/ccompilerbin/
    Main.kt                  # fun main(args: Array<String>)
    CliParser.kt             # Same structure as asmcompiler's CliParser
    CCompilerTool.kt         # Orchestrates compile / write / run / disassemble
```

### 4.4 `vminterp` module — new

```
vminterp/
    build.gradle
    src/commonMain/kotlin/com/hiperbou/vm/vminterp/
        InterpOptions.kt     # data class InterpOptions(...) — shared across platforms
    src/jvmMain/kotlin/com/hiperbou/vm/vminterp/
        Main.kt              # fun main(args: Array<String>)
        CliParser.kt         # Parses: inputFile, --disassemble, --help, --version
        InterpTool.kt        # Orchestrates read / run / disassemble (uses java.io.File)
```

`vminterp` CLI flags (subset — no `-o`, no `--run`):

| Flag | Short | Description |
|------|-------|-------------|
| `--disassemble` | `-d` | Print disassembly instead of running |
| `--help` | `-h` | Show usage |
| `--version` | `-v` | Show version |

Default behavior: read `.vmb` and execute.

### 4.5 `settings.gradle` changes

```groovy
include 'vm', 'assembler', 'editor', 'vmbin', 'conversation',
        'ccompiler', 'ccompilerbin',
        'asmcompiler', 'vminterp'          // add these two
```

---

## 5. Build Configuration

### 5.1 Shadow plugin — `settings.gradle` `pluginManagement`

Add to the `plugins` block in `pluginManagement`:
```groovy
id 'com.github.johnrengelman.shadow' version '7.1.2'
```

### 5.2 `asmcompiler/build.gradle` (representative — `vminterp` is identical shape)

```groovy
plugins {
    id 'org.jetbrains.kotlin.multiplatform' version '1.7.10'
    id 'com.github.johnrengelman.shadow'
}

group = 'com.hiperbou.vm'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

kotlin {
    jvm() {
        compilations.all {
            kotlinOptions.jvmTarget = '1.8'
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    // Future Native targets can be added here, e.g.:
    // linuxX64 { binaries.executable() }
    // macosX64 { binaries.executable() }
    // mingwX64 { binaries.executable() }

    sourceSets {
        commonMain {
            dependencies {
                implementation project(':vm')
            }
        }
        commonTest {
            dependencies {
                implementation kotlin('test')
            }
        }
        jvmMain {
            dependencies {}
        }
    }
}

shadowJar {
    archiveBaseName.set('asmcompiler')
    archiveClassifier.set('')
    archiveVersion.set('')
    configurations = [project.configurations.jvmRuntimeClasspath]
    from(kotlin.targets.jvm.compilations.main.output.allOutputs)
    manifest {
        attributes 'Main-Class': 'com.hiperbou.vm.asmcompiler.MainKt'
    }
}
```

`vminterp/build.gradle` is identical with `archiveBaseName = 'vminterp'`, `Main-Class = 'com.hiperbou.vm.vminterp.MainKt'`, and `:vm` dependency only (no `:ccompiler`).

### 5.3 `ccompilerbin/build.gradle` changes

- Add `id 'com.github.johnrengelman.shadow'` to plugins block.
- Add a `shadowJar` task targeting the JVM runtime classpath of the KMP module:

```groovy
shadowJar {
    archiveBaseName.set('ccompiler')
    archiveClassifier.set('')
    archiveVersion.set('')
    configurations = [project.configurations.jvmRuntimeClasspath]
    from(kotlin.targets.jvm.compilations.main.output.allOutputs)
    manifest {
        attributes 'Main-Class': 'com.hiperbou.vm.ccompilerbin.MainKt'
    }
}
```

---

## 6. Key Interface / Data Flow

### Compile-only path (`-o output.vmb`)
```
File.readText()
  → Compiler/CCompiler.compile()    : IntArray
  → BytecodeWriter.write()          : ByteArray
  → BytecodeFile.writeTo(File)
```

### Run path (default or `--run`)
```
IntArray
  → CPU(instructions)
  → cpu.appendDecoder(PrintDecoder)
  → cpu.run()
```

### Disassemble path (`--disassemble`)
```
IntArray
  → ProgramDecompiler(OpcodeInformationChain(Core, Print)).decompile()  : List<Instruction>
  → Disassembler().disassemble()    : String
  → println()
```

### Interpreter read path
```
File
  → BytecodeFile.readFrom(File)     : ByteArray
  → BytecodeReader.read()           : IntArray
  → [run or disassemble path above]
```

---

## 7. Error Handling

- All tools catch exceptions from compilers and the CPU, print a user-friendly message to `stderr`, and call `exitProcess(1)`.
- `BytecodeReader` throws `InvalidBytecodeFileException` (subclass of `Exception`) for malformed files.
- No raw stack traces are shown to the user; only the `.message` of caught exceptions.
- Exit code `0` = success; `1` = any error.

---

## 8. Delivery Phases

### Phase 1 — Bytecode format utility in `vm`
- Add `BytecodeFormat.kt`, `BytecodeWriter.kt`, `BytecodeReader.kt` to `vm/commonMain`
- Add `BytecodeFile.kt` to `vm/jvmMain` for `java.io.File` I/O
- Unit tests in `vm/commonTest`: write then read round-trip; bad magic throws; bad version throws

### Phase 2 — `asmcompiler` standalone JAR
- Create `asmcompiler` module with `build.gradle` (Shadow plugin, `:vm` dependency)
- Implement `CliParser`, `ToolOptions`, `AsmTool`, `Main`
- Add `asmcompiler` to `settings.gradle`
- Verify: `./gradlew :asmcompiler:shadowJar` succeeds
- Manual test with a sample `.asm` file

### Phase 3 — `ccompilerbin` JVM entry point
- Add `jvmMain` sources to `ccompilerbin`: `CliParser`, `CCompilerTool`, `Main`
- Add Shadow JAR task and Shadow plugin to `ccompilerbin/build.gradle`
- Verify: `./gradlew :ccompilerbin:shadowJar` succeeds
- Manual test with a sample `.c` file

### Phase 4 — `vminterp` standalone JAR
- Create `vminterp` module with `build.gradle` (Shadow plugin, `:vm` dependency)
- Implement `CliParser`, `InterpTool`, `Main`
- Add `vminterp` to `settings.gradle`
- Verify: `./gradlew :vminterp:shadowJar` succeeds
- Manual end-to-end test: compile with `asmcompiler` → run with `vminterp`; compile with `ccompilerbin` → run with `vminterp`

---

## 9. Verification

### Build commands
```
./gradlew :vm:build
./gradlew :asmcompiler:shadowJar
./gradlew :ccompilerbin:shadowJar
./gradlew :vminterp:shadowJar
```

### Acceptance test matrix (matches PRD §9)

| Command | Expected |
|---------|----------|
| `java -jar asmcompiler.jar hello.asm` | Compiles + runs |
| `java -jar asmcompiler.jar hello.asm -o hello.vmb` | Produces valid `.vmb` file |
| `java -jar asmcompiler.jar hello.asm --disassemble` | Prints disassembly |
| `java -jar asmcompiler.jar --help` | Prints usage, exit 0 |
| `java -jar ccompiler.jar hello.c` | Compiles + runs |
| `java -jar ccompiler.jar hello.c -o hello.vmb` | Produces valid `.vmb` file |
| `java -jar vminterp.jar hello.vmb` | Executes binary |
| `java -jar vminterp.jar hello.vmb --disassemble` | Prints disassembly |
| `java -jar vminterp.jar bad.vmb` | Error message, exit 1 |
| `java -jar asmcompiler.jar missing.asm` | Error message, exit 1 |
