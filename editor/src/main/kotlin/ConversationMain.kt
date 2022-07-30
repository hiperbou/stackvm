import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.Instructions.CALL
import com.hiperbou.vm.Instructions.HALT
import com.hiperbou.vm.Instructions.JMP
import com.hiperbou.vm.Instructions.NOP
import com.hiperbou.vm.Instructions.PUSH
import com.hiperbou.vm.Instructions.RET
import com.hiperbou.vm.Instructions.WRITE
import com.hiperbou.vm.dsl.PROGRAM
import com.hiperbou.vm.dsl.program
import com.hiperbou.vm.memory.*
import com.xemantic.kotlin.swing.mainFrame
import com.xemantic.kotlin.swing.verticalPanel
import java.awt.Color
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


class ConversationMain {
    class ConversationDemo {

        lateinit var labelChar:JLabel
        lateinit var labelText:JLabel
        lateinit var startButton:JButton
        lateinit var nextButton:JButton

        fun app(): JPanel {
            val panel =
                verticalPanel {
                    grid(1, 2) {
                        labelChar = label("PRESS START: ") {
                            horizontalAlignment = SwingConstants.RIGHT
                        }
                        labelText = label("AND THEN PRESS NEXT TO CONTINUE THE CONVERSATION")
                    }
                    grid(1, 2) {
                        startButton = button("Start") {
                            addActionListener { onStart() }
                        }
                        nextButton = button("Next") {
                            setEnabled(false)
                            addActionListener { onNext() }
                        }
                    }
                }
            return panel
        }

        val chars = mutableListOf<String>()
        val texts = mutableListOf<String>()

        inner class Conversation {
            init {
                chars.clear()
                texts.clear()
            }
            fun createCharacter(name:String):Int {
                chars.add(name)
                return chars.lastIndex
            }

            var program:String = ""

            fun append(text:String) {
                program = program + "\n" + text
            }

            fun character(id:Int) {
                println("character selected ${chars[id]}")
                append("""
                    PUSH $id
                    CALL setCharacter 
                """.trimIndent())
            }

            fun PROGRAM.characterBin(id:Int, setCharacterAddress:Int = 2) {
                write(
                    PUSH, id,
                    CALL, setCharacterAddress
                )
            }

            fun talk(what:String){
                println(what)
                texts.add(what)
                append("""
                     PUSH ${texts.lastIndex}  
                     CALL say
                """.trimIndent())
            }

            fun PROGRAM.talkBin(what:String, sayAddress:Int = 5) {
                texts.add(what)
                write(
                    PUSH, texts.lastIndex,
                    CALL, sayAddress
                )
            }

            fun saveMemory(index: Int, value:Int) {
                append("""  
                    PUSH $value
                    WRITE $index
                """.trimIndent())
            }

            fun PROGRAM.saveMemoryBin(index: Int, value:Int) {
                write(
                    PUSH, value,
                    WRITE, index
                )
            }

            fun defineLabel(label:String) {
                append("""  
                    $label:
                """.trimIndent())
            }

            fun gotoLabelIfTrue(index: Int, label:String) {
                append("""  
                    READ $index
                    JIF $label
                """.trimIndent())
            }

            fun halt() {
                append("""  
                    HALT
                """.trimIndent())
            }

            fun PROGRAM.haltBin() {
                write(
                    HALT
                )
            }

            fun end() {
                append("""
                    HALT
                                        
                    setCharacter:
                    WRITE 0
                    RET
    
                    say:
                    WRITE 1
                    RET
                """.trimIndent())
            }

            fun PROGRAM.startBin() {
                write(
                    JMP, NOP, //JUMP TO CODE START
                    //setCharacter:
                    WRITE, 0,
                    RET,
                    //say:
                    WRITE, 1,
                    RET
                )
                overwriteWithCurrentSize(1)
            }

            fun PROGRAM.endBin() {
                write(
                    HALT
                )
            }

            inner class Character(val name:String) {
                private val id = createCharacter(name)

                context(PROGRAM)
                fun say(what: String) {
                    characterBin(id)
                    talkBin(what)
                }

                context(PROGRAM)
                operator fun String.unaryPlus() {
                    say(this)
                }
            }

            inner class CharacterStr(val name:String) {
                private val id = createCharacter("Bob")

                fun say(what:String) {
                    character(id)
                    talk(what)
                }

                operator fun String.unaryPlus() {
                    say(this)
                }
            }

            fun label(label:String, block: ConversationDemo.Conversation.() -> Unit) {
                defineLabel(label)
                block()
            }

            fun conversation(init: PROGRAM.() -> Unit): PROGRAM {
                return program {
                    startBin()
                    init()
                }
            }

            fun startBin():IntArray {
                val ADDRESS_ALREADY_TALKED = 2

                val bob = Character("Bob")
                val alice = Character("Alice")

                return conversation {
                    bob.say("Hi Alice. How are you?")

                    ifElseCondition({
                        readMemory(ADDRESS_ALREADY_TALKED)
                    }, {
                        with(alice) {
                            +"We already talked"
                            +"Leave me alone"
                        }
                        bob.say("Uh... ok. :(")
                    }, {
                        with(alice) {
                            +"Hi Bob! I'm fine."
                            +"Thank you!"
                        }
                        writeMemory(ADDRESS_ALREADY_TALKED, 1)
                    })
                }.build()
            }

            private fun PROGRAM.characterTalk(bob: Int, what: String) {
                characterBin(bob)
                talkBin(what)
            }

            fun start():String {
                program = ""

                val ADDRESS_ALREADY_TALKED = 2
                val LABEL_ALREADY_TALKED = "alreadyTalked"

                val bob = CharacterStr("Bob")
                val alice = CharacterStr("Alice")

                with(bob) {
                    +"Hi Alice. How are you?"

                    with(alice) {
                        gotoLabelIfTrue(ADDRESS_ALREADY_TALKED, LABEL_ALREADY_TALKED)

                        +"Hi Bob! I'm fine."
                        +"Thank you!"

                        saveMemory(ADDRESS_ALREADY_TALKED, 1)
                        halt()

                        label(LABEL_ALREADY_TALKED) {
                            +"We already talked"
                            +"Leave me alone"
                        }
                    }
                    +"Uh... ok. :("
                }
                end()

                return program
            }
        }

        private lateinit var cpu:CPU
        private val compiler = LittleCompiler()
        private var pauseCPU = false

        lateinit var memory:Memory

        private fun onStart() {
            startButton.setEnabled(false)

            val conv = Conversation()
            //val program = conv.start()
            //println(program)

            //val programParsed = compiler.parseProgram(program)
            val programParsed = conv.startBin()

            fun getMapper():MemoryMapper{
                return MemoryMapper(IntArray(UByte.MAX_VALUE.toInt())).apply {
                    map(ConversationDevice.builder(this@ConversationDemo))
                }
            }

            memory = if(::memory.isInitialized) memory else getMapper()
            cpu = compiler.stepProgram(programParsed, memory)!!
            onNext()
        }

        private fun onNext() {
            pauseCPU = false
            while(!cpu.isHalted() && !pauseCPU) {
                compiler.step(cpu)
            }
            refreshUI()
        }

        private fun refreshUI() {
            if (cpu.isHalted()) {
                startButton.setEnabled(true)
                nextButton.setEnabled(false)
            } else {
                nextButton.setEnabled(true)
            }
            labelChar.revalidate()
            labelText.revalidate()
        }

        fun changeTextColor(color: Color) {
            labelChar.foreground = color
            labelText.foreground = color
        }

        fun updateCharacter(index: Int) {
            changeTextColor(when(index){
                0 -> Color.BLUE
                else -> Color.RED
            })
            println("updateCharacter: $index ${chars[index]}")
            labelChar.text = "${chars[index]}: "
        }

        fun updateText(index: Int){
            labelText.text = texts[index]
            pauseCPU = true
        }

        class ConversationDevice(private val memory: Memory, val conversationDemo:ConversationDemo): Memory by memory {
            companion object {
                const val Character = 0
                const val Text = 1

                const val size = 2

                fun builder(conversationDemo:ConversationDemo) = DeviceMapper({ size }) { ConversationDevice(it, conversationDemo) }
            }

            override fun size() = size

            private inner class ChangeCharacterMemoryRegister(private val memory: Memory): WriteMemoryRegister {
                override fun onWrite(value: Int) {
                    println("VM: Changing to character $value")
                    memory[Character] = value
                    conversationDemo.updateCharacter(value)
                }
            }

            private inner class ChangeTextMemoryRegister(private val memory: Memory): WriteMemoryRegister {
                override fun onWrite(value: Int) {
                    println("VM: Changing text to $value")
                    memory[Text] = value
                    conversationDemo.updateText(value)
                }
            }

            private val changeCharacterMemoryRegister = ChangeCharacterMemoryRegister(memory)
            private val changeTextMemoryRegister = ChangeTextMemoryRegister(memory)

            override operator fun set(index: Int, value:Int) {
                when(index) {
                    Character -> changeCharacterMemoryRegister.onWrite(value)
                    Text -> changeTextMemoryRegister.onWrite(value)
                    else -> {}
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = mainFrame("conversationVM") {
            contentPane = ConversationDemo().app()
        }
    }
}