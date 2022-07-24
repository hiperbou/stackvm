import com.hiperbou.vm.CPU
import com.hiperbou.vm.Frame
import com.hiperbou.vm.compiler.Compiler
import com.hiperbou.vm.compiler.DebugProgramWriter
import com.hiperbou.vm.decompiler.ProgramDecompiler
import com.hiperbou.vm.disassembler.Disassembler
import com.hiperbou.vm.memory.Memory
import com.hiperbou.vm.memory.MemoryMapper
import com.hiperbou.vm.state.CPUState
import com.hiperbou.vm.state.saveState
import com.jhe.hexed.JHexEditor
import com.xemantic.kotlin.swing.*
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import java.util.*
import javax.swing.*

class LittleCompiler {
    private val compiler = Compiler()

    private val decompiler = ProgramDecompiler()
    private val disassembler = Disassembler()

    private var lineOpcodeMap = mutableMapOf<Int,Int>()

    fun parseProgram(source: String): IntArray {
        lineOpcodeMap.clear()
        return compiler.generateProgram(source.byteInputStream().reader(), DebugProgramWriter(
            lineOpcodeMap = lineOpcodeMap
        ))
    }

    fun getDebugInfo() = lineOpcodeMap

    fun disassembly(program: IntArray): String {
        val decompilation = decompiler.decompile(program)
        return disassembler.disassemble(decompilation)
    }

    private fun CPUState<Int, Frame>.format():String {
        return with(StringBuilder()){
            append("Halted: ")
            append(halted)
            append("\ninstructionAddress: ")
            append(instructionAddress)
            append("\nstack: ")
            append(stack.toString())
            append("\nframe: ")
            append(frames.peek())
            append("\nglobals: ")
            append(globals)
        }.toString()
    }

    data class ProgramResult (val text:String, val memory:IntArray)

    fun runProgram(program: IntArray):ProgramResult? {
        try {
            val cpu = CPU(program)
            cpu.run()
            val state = cpu.saveState()
            return ProgramResult(state.format(), state.memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun runProgram(program: IntArray, memory: Memory):ProgramResult? {
        try {
            val cpu = CPU(instructions = program, memory = memory)
            cpu.run()
            val state = cpu.saveState()
            return ProgramResult(state.format(), state.memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun stepProgram(program: IntArray):CPU? {
        try {
            return CPU(program)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun stepProgram(program: IntArray, memory: Memory):CPU? {
        try {
            return CPU(instructions = program, memory = memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun step(cpu: CPU):ProgramResult? {
        try {
            if (!cpu.isHalted()) cpu.step()
            return state(cpu)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }

    fun state(cpu: CPU):ProgramResult? {
        try {
            val state = cpu.saveState()
            return ProgramResult(state.format(), state.memory)
        } catch (e:Exception){
            println(e.toString())
        }
        return null
    }
}

class TextEditorDemo {

    lateinit var codeArea:RSyntaxTextArea
    lateinit var compiledArea:RSyntaxTextArea
    lateinit var runArea:RSyntaxTextArea
    lateinit var hexEditor: JHexEditor

    val compiler = LittleCompiler()

    val loadedText3 = """
        // - Bob  
        PUSH 0 // Bob
        CALL setCharacter // -

        //	Hi Alice. How are you?
        PUSH 0 
        CALL say

        //- Alice  
        PUSH 1 // Alice
        CALL setCharacter // -

        //	Hi Bob! I'm fine.
        PUSH 1 
        CALL say
        //	Thank you!
        PUSH 2 
        CALL say

        HALT

        .MEMORY
        Reserved[16]
        Conversation {
        	character
        	text	
        }

        SpriteTable {
        	Sprite[64] {
        		x
        		y
        		graph	
        	}
        }
        .END

        setCharacter:
        WRITE Conversation.character //STORE TALKING PLAYER FROM THE STACK
        PUSH 1
        WRITE SpriteTable.Sprite[0].graph 
        PUSH 0
        WRITE SpriteTable.Sprite[1].graph 
        RET

        say:
        WRITE Conversation.text //STORE TEXT FROM THE STACK
        RET
    """.trimIndent()

    val loadedText = """
        // - Bob  
        PUSH 0 // Bob
        CALL setCharacter // -

        //	Hi Alice. How are you?
        PUSH 0 
        CALL say

        //- Alice  
        PUSH 1 // Alice
        CALL setCharacter // -

        //	Hi Bob! I'm fine.
        PUSH 1 
        CALL say
        //	Thank you!
        PUSH 2 
        CALL say

        HALT


        setCharacter:
        GSTORE 0 //STORE TALKING PLAYER FROM THE STACK
        RET

        say:
        GSTORE 1 //STORE TEXT FROM THE STACK
        RET

    """.trimIndent()
    val loadedText2 = """
                PUSH 6
                PUSH 4
                CALL max
                PUSH 6
                PUSH 4
                CALL maxOpcode
                HALT
                
                max:
                STORE 1
                STORE 0
                LOAD 0
                LOAD 1
                GTE
                JIF exit
                LOAD 1
                RET
                
                exit:
                LOAD 0
                RET
                
                maxOpcode:
                MAX
                RET
    """.trimIndent()


    fun app():JPanel {
        val panel =
            verticalPanel {
                flowPanel {
                    button("Build") {
                        addActionListener { onBuild() }
                    }
                    button("Run") {
                        addActionListener { onRun() }
                    }
                    button("Step"){
                        addActionListener { onStep() }
                    }
                }
                /*grid(1, 2) {
                    label("Result:")
                    textArea("result", 3)
                }*/

                grid(1, 3) {
                    rtextScrollPane(
                        rsyntaxTextArea(40, 60) {
                            syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA
                            isCodeFoldingEnabled = true
                            codeArea = this
                            text = loadedText
                        }
                    ).apply {
                        val url =  javaClass.classLoader.getResource("bookmark.png")
                        gutter.setBookmarkIcon(ImageIcon(url))
                        gutter.setBookmarkingEnabled(true)

                        //gutter.toggleBookmark(3)
                        //gutter.lineNumberColor = Color.RED //Text color
                        //gutter.activeLineRangeColor = Color.GREEN
                        //gutter.borderColor = Color.BLUE //vertical right border color
                        //gutter.currentLineNumberColor = Color.BLUE // Text color
                    }

                    rtextScrollPane(
                        rsyntaxTextArea(20, 60) {
                            syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA
                            isCodeFoldingEnabled = true
                            isEditable = false
                            compiledArea = this
                        }
                    )
                    verticalPanel {
                        rtextScrollPane(
                            rsyntaxTextArea(14, 60) {
                                syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVA
                                isCodeFoldingEnabled = true
                                isEditable = false
                                runArea = this
                            }
                        )
                        val ar = ByteArray(16 * 16 * 100)
                        Arrays.fill(ar, 0.toByte())
                        hexEditor = JHexEditor(ar)
                        add(hexEditor)
                    }

                }
            }
        return panel
    }

    data class BuildResult(val program: IntArray, val debug:Map<Int, Int>)
    fun onBuild():BuildResult {
        currentCPU = null //reset the saved cpu for stepping
        var program = IntArray(0)
        try {
            program = compiler.parseProgram(codeArea.text)
            compiledArea.text = compiler.disassembly(program)
        } catch (e:Exception) {
            compiledArea.text = e.message
        }
        return BuildResult(program, compiler.getDebugInfo())
    }

    fun updateHexEditor(state:LittleCompiler.ProgramResult) {
        val container = hexEditor.parent
        container.remove(hexEditor)

        fun write4BytesToBufferReverse(buffer: ByteArray, offset: Int, data: Int) {
            buffer[offset + 3] = (data shr 0).toByte()
            buffer[offset + 2] = (data shr 8).toByte()
            buffer[offset + 1] = (data shr 16).toByte()
            buffer[offset + 0] = (data shr 24).toByte()
        }

        fun write4BytesToBufferNative(buffer: ByteArray, offset: Int, data: Int) {
            buffer[offset + 0] = (data shr 0).toByte()
            buffer[offset + 1] = (data shr 8).toByte()
            buffer[offset + 2] = (data shr 16).toByte()
            buffer[offset + 3] = (data shr 24).toByte()
        }

        fun write4BytesToBuffer(buffer: ByteArray, offset: Int, data: Int) = write4BytesToBufferReverse(buffer, offset, data)

        fun read4BytesFromBuffer(buffer: ByteArray, offset: Int): Int {
            return (buffer[offset + 3].toInt() shl 24) or
                    (buffer[offset + 2].toInt() and 0xff shl 16) or
                    (buffer[offset + 1].toInt() and 0xff shl 8) or
                    (buffer[offset + 0].toInt() and 0xff)
        }

        fun IntArray.toByteArray():ByteArray {
            val buffer = ByteArray(size * 4)
            this.forEachIndexed { index, it ->
                write4BytesToBuffer(buffer, index * 4, it)
            }
            return buffer
        }

        hexEditor = JHexEditor(state.memory.toByteArray())
        container.add(hexEditor)
        container.revalidate()
    }

    fun onRun() {
        val program = onBuild().program
        runArea.text = compiledArea.text
        val state = compiler.runProgram(program)
        if (state!=null) {
            runArea.text = state.text
            updateHexEditor(state)
        }
    }

    var currentCPU: CPU? = null
    var debugInfo:Map<Int, Int> = mapOf()
    fun onStep() {
        fun updateRunningCodeLine(){
            val lineNumber = debugInfo.getOrDefault(currentCPU!!.instructionAddress, 0)
            //println("Line number: $lineNumber")
            RXTextUtilities.gotoStartOfLine(codeArea, lineNumber)
            //editorGutter.toggleBookmark(lineNumber - 1)
            //compiledArea.caretPosition = lineNumber
            //compiledArea.currentLineHighlightColor = Color.GREEN
        }

        if (currentCPU == null) {
            with(onBuild()) {
                currentCPU = compiler.stepProgram(program)
                debugInfo = debug
            }
            compiler.state(currentCPU!!)?.let {
                runArea.text = it.text
                updateHexEditor(it)
            }

            updateRunningCodeLine()
        } else {
            try {
                compiler.step(currentCPU!!)?.let {
                    runArea.text = it.text
                    updateHexEditor(it)
                }
                if (currentCPU!!.isHalted()) return
                updateRunningCodeLine()
            } catch (e:Throwable) {
                runArea.text = e.message
            }
        }

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = mainFrame("daniVM") {
            contentPane = TextEditorDemo().app()
        }
    }
}
