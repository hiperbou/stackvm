import com.hiperbou.vm.CPU
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
import java.awt.Dialog
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

        val optionButtons = mutableListOf<JButton>()

        fun app(): JPanel {
            val panel =
                verticalPanel {
                    grid(1, 1) {
                        labelChar = label("PRESS START: ") {
                            horizontalAlignment = SwingConstants.RIGHT
                        }
                        labelText = label("AND THEN PRESS NEXT TO CONTINUE THE CONVERSATION")
                    }
                    grid(5, 1) {
                        repeat(5) {index ->
                            optionButtons.add(button("Option $index") {
                                addActionListener { onOptionButton(index,this) }
                                setEnabled(false)
                            })
                        }
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
        val options = mutableListOf<Conversation.DialogOption>()


        class Label(var _id:String = "") {
            fun getId():String {
                if (_id.isEmpty()){
                    _id = "label_${labelID++}"
                }
                return _id
            }

            context(Conversation)
            operator fun invoke(block: ConversationDemo.Conversation.() -> Unit) {
                label(this, block)
            }

            context(Conversation)
            fun gotoIfTrue(memory:MemoryAddress) {
                gotoLabelIfTrue(memory, this)
            }

            companion object{
                var labelID = 0
            }
        }



        class MemoryAddress(val address:Int = -1) {

            context(Conversation)
            fun set(value: Int):MemoryAddress {
                saveMemory(this, value)
                return this
            }

            context(Conversation)
            fun set(boolean: Boolean):MemoryAddress {
                return set(if (boolean) 1 else 0)
            }

            context(Conversation)
            operator fun invoke(block: ConversationDemo.Conversation.() -> Unit) {
                //label(this, block)
            }

        }


        inner class Conversation {
            init {
                chars.clear()
                texts.clear()
                options.clear()
            }
            fun createCharacter(name:String):Int {
                chars.add(name)
                return chars.lastIndex
            }

            fun createOption(option:DialogOption):Int {
                options.add(option)
                return options.lastIndex
            }

            var program:String = ""

            fun append(text:String) {
                program = program + "\n" + text
            }

            fun emitSetCharacter(id:Int) {
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

            fun emitTalk(textIndex:Int) {
                append("""
                     PUSH ${textIndex}  
                     CALL say
                """.trimIndent())
            }

            fun talk(what:String){
                println(what)
                texts.add(what)
                emitTalk(texts.lastIndex)
            }

            fun PROGRAM.talkBin(what:String, sayAddress:Int = 5) {
                texts.add(what)
                write(
                    PUSH, texts.lastIndex,
                    CALL, sayAddress
                )
            }

            fun emitSaveMemory(index: Int, value:Int) {
                append("""  
                    PUSH $value
                    WRITE $index
                """.trimIndent())
            }

            fun saveMemory(memory: MemoryAddress, value:Int) {
                emitSaveMemory(memory.address, value)
            }

            fun PROGRAM.saveMemoryBin(index: Int, value:Int) {
                write(
                    PUSH, value,
                    WRITE, index
                )
            }

            fun emitDefineLabel(label:String) {
                append("""  
                    $label:
                """.trimIndent())
            }

            fun emitGotoLabelIfTrue(index: Int, label:String) {
                append("""  
                    READ $index
                    JIF $label
                """.trimIndent())
            }

            fun emitEnableOption(option: DialogOption, enabled: Int) {
                append("""
                    PUSH $enabled
                    WRITE memoryAddressOptions + ${option.id}
                """.trimIndent())
            }

            fun emitShowOptions() {
                append("""
                    CALL showOptions
                    CALL getSelectedOption
                    JMP optionsSwitch
                """.trimIndent())
            }

            fun emitBuildOptions() {
                append("""
                    JMP endOptionsSwitch
                    
                    optionsSwitch:
                    WRITE memoryAddressSelectedOption
                """.trimIndent())

                options.forEachIndexed { index, it->
                    append("""
                    //case: ${it.text}
                    READ memoryAddressSelectedOption
                    PUSH ${it.id}
                    EQ
                    JIF ${it.label.getId()}
                """.trimIndent())
                }

                append("""
                    //else	
                    JMP endOptionsSwitch
                    
                    endOptionsSwitch:
                """.trimIndent())
            }

            fun gotoLabelIfTrue(index:Int, label:Label) {
                emitGotoLabelIfTrue(index, label.getId())
            }

            fun gotoLabelIfTrue(memory:MemoryAddress, label:Label) {
                emitGotoLabelIfTrue(memory.address, label.getId())
            }

            fun emitHalt() {
                append("""  
                    HALT
                """.trimIndent())
            }

            fun halt() {
                emitHalt()
            }

            fun PROGRAM.haltBin() {
                write(
                    HALT
                )
            }

            fun emitStart() {
                append("""
                    
                    memoryAddressSetCharacter: 0
                    memoryAddressSay: 1
                    
                    memoryAddressOptions: 2
                    memoryAddressShowOptions: memoryAddressOptions + 16
                    memoryAddressSelectedOption: memoryAddressShowOptions + 1
                    
                """.trimIndent())
            }

            fun emitEnd() {
                append("""
                    HALT
                    
                    setCharacter:
                    WRITE memoryAddressSetCharacter
                    RET
    
                    say:
                    WRITE memoryAddressSay
                    RET
                    
                    showOptions:
                    PUSH 1
                    WRITE memoryAddressShowOptions
                    RET
                    
                    getSelectedOption:
                    READ memoryAddressShowOptions
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
                infix fun say(what: String) {
                    characterBin(id)
                    talkBin(what)
                }

                context(PROGRAM)
                operator fun String.unaryPlus() {
                    say(this)
                }

                context(PROGRAM)
                operator fun String.unaryMinus() {
                    say(this)
                }

                context(PROGRAM)
                operator fun invoke(vararg what:String) {
                    what.forEach { say(it) }
                }
            }

            inner class CharacterStr(name:String) {
                private val id = createCharacter(name)

                infix fun say(what:String) {
                    emitSetCharacter(id)
                    talk(what)
                }

                operator fun String.unaryPlus() {
                    say(this)
                }

                operator fun String.unaryMinus() {
                    say(this)
                }

                operator fun invoke(vararg what:String) {
                    what.forEach { say(it) }
                }

                context(Conversation)
                operator fun invoke(block: ConversationDemo.Conversation.() -> Unit) {
                    emitSetCharacter(id)
                    block()
                }

                operator fun minus(what:String):CharacterStr {
                    say(what)
                    return this
                }

                operator fun plus(what:String):CharacterStr {
                    say(what)
                    return this
                }

                operator fun unaryMinus() {
                    emitSetCharacter(id)
                }
            }

            inner class DialogOption(val text:String, var enabled:Int) {
                val id = createOption(this)

                val label = Label()//("option_${text.replace(" ","_")}")

                fun enable() = emitEnableOption(this, 1)
                fun disable() = emitEnableOption(this, 0)

                context(Conversation)
                operator fun invoke(block: ConversationDemo.Conversation.(DialogOption) -> Unit) {
                    label{}
                    block(this)
                }

                init {
                    emitEnableOption(this, enabled)
                }

                override fun toString() = " * $id:$text[$enabled]"
            }

            fun label(label:String, block: ConversationDemo.Conversation.() -> Unit) {
                emitDefineLabel(label)
                block()
            }

            fun label(label:Label, block: ConversationDemo.Conversation.() -> Unit) {
                label(label.getId(), block)
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


            operator fun String.unaryPlus() {
                talk(this)
            }

            operator fun String.unaryMinus() {
                talk(this)
            }

            fun start():String {
                program = ""
                emitStart()

                //0 - set character
                //1 - say
                //2-19 OptionsDevice
                val variableAlreadyTalked = MemoryAddress(32)
                val variableMoreConversation = MemoryAddress(33)

                val labelAlreadyTalked = Label()
                val labelMoreConversation = Label()

                val bob = CharacterStr("Bob")
                val alice = CharacterStr("Alice")

                fun conversation1() {
                    -bob
                    -"Hi Alice. How are you?"

                    labelMoreConversation.gotoIfTrue(variableMoreConversation)
                    labelAlreadyTalked.gotoIfTrue(variableAlreadyTalked)

                    -alice
                    -"Hi Bob! I'm fine."
                    -"Thank you!"

                    variableAlreadyTalked.set(1)
                    halt()

                    labelAlreadyTalked {
                        alice {
                            -"We already talked"
                            -"Leave me alone"
                        }
                        variableMoreConversation.set(true)
                        halt()
                    }

                    labelMoreConversation {
                        alice - "What do you want now?"
                        bob - "Well, this is just another branch"
                        alice - "So?"
                        bob - "That it's interesting enough how this can be used to make some conversations"
                        alice {
                            -"But it's difficult"
                            -"You have to write so much code to make different characters talk."
                        }
                        bob - "What could I do about that?"
                        alice - "Just think on some Kotlin magic."
                        bob - "what about this?"
                        alice - "So many methods, just decide one."
                    }

                    bob - "Uh... ok. :("
                }

                //conversation1()

                fun buildOptions() {
                    emitBuildOptions()
                }

                fun showOptions(){
                    emitShowOptions()
                }


                fun option(text:String, enabled: Int) = DialogOption(text, enabled)

                fun conversationWithOptions() {
                    val optionSaludar = option("hola", 1)
                    val optionRobar = option("dame el oro", 1)
                    val optionRobarConViolencia = option("dame el oro ahora", 0)
                    val optionAdios = option("hasta luego", 1)
                    buildOptions()

                    - bob
                        -"Ahem..."

                    showOptions()

                    optionSaludar {
                        bob - "¿Hola que pasa?"
                        alice - "Buenos días."

                        optionSaludar.disable()
                        showOptions()
                    }

                    optionRobar {
                        bob - "¡Oh, vaya! ¿Qué es eso de allí?"
                        alice - "hmm?"
                        bob - "Esto no va a ser fácil..."

                        optionRobarConViolencia.enable()
                        showOptions()
                    }

                    optionRobarConViolencia {
                        bob - "¡Dame el oro o te quemo con el mechero!"
                        alice - "911"
                        bob - "¡Oh no!"
                        halt()
                    }

                    optionAdios {
                        bob - "Hasta luego."
                        alice - "Piérdete."
                        halt()
                    }
                }
                conversationWithOptions()

                emitEnd()
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

            val program = conv.start()
            println(program)
            val programParsed = compiler.parseProgram(program)

            //val programParsed = conv.startBin()


            fun getMapper():MemoryMapper{
                return MemoryMapper(IntArray(UByte.MAX_VALUE.toInt())).apply {
                    map(ConversationDevice.builder(this@ConversationDemo))
                    map(ConversationOptionsDevice.builder(this@ConversationDemo))
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

        private fun onOptionButton(buttonIndex:Int, button:JButton) {
            println("Button pressed $buttonIndex")

            val availableOptions = options.filter { it.enabled != 0 }
            selectedOption = availableOptions[buttonIndex]
            disableOptionButons()
            onNext()
        }
        private fun disableOptionButons(){
            optionButtons.forEach {
                it.setEnabled(false)
            }
        }

        fun updateOption(index:Int, value:Int) {
            println("Updating option $index to $value")
            options[index].enabled = value
        }

        private lateinit var selectedOption:Conversation.DialogOption
        fun showOptions() {
            println("Show options!")
            val availableOptions = options.filter { it.enabled != 0 }
            availableOptions.forEach { println(it) }
            pauseCPU = true

            availableOptions.forEachIndexed { index, it ->
                optionButtons[index].apply {
                    text = it.text
                    setEnabled(it.enabled == 1)
                }
            }
            selectedOption = availableOptions.random()
        }

        fun getSelectedOption():Int {
            println("getSelectedOption")
            println("selected option $selectedOption")
            return selectedOption.id
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

        class ConversationOptionsDevice(private val memory: Memory, val conversationDemo:ConversationDemo): Memory by memory {
            companion object {
                private const val maxOptions = 16
                private const val optionSize = 1

                const val ShowOptions = maxOptions * optionSize
                const val SelectedOption = ShowOptions + 1

                const val size = SelectedOption + 1

                fun builder(conversationDemo:ConversationDemo) = DeviceMapper({ size }) { ConversationOptionsDevice(it, conversationDemo) }
            }

            override fun size() = size


            private inner class ChangeShowOptionsMemoryRegister(private val memory: Memory): ReadWriteMemoryRegister {
                override fun onWrite(value: Int) {
                    println("VM: Showing dialogue options")
                    memory[ShowOptions] = value // we don't care about the value at all
                    conversationDemo.showOptions()
                }

                override fun onRead() {
                    println("VM: reading selected dialogue option")
                    memory[ShowOptions] = conversationDemo.getSelectedOption()
                }
            }

            private val changeShowOptionsMemoryRegister = ChangeShowOptionsMemoryRegister(memory)

            override operator fun set(index: Int, value:Int) {
                when(index) {
                    ShowOptions -> changeShowOptionsMemoryRegister.onWrite(value)
                    SelectedOption -> {
                        println("VM: Changing selected option to $value")
                        memory[index] = value
                    }
                    else -> {
                        println("VM: Changing option $index to $value")
                        memory[index] = value
                        conversationDemo.updateOption(index, value)
                    }
                }
            }

            override fun get(index: Int): Int {
                when(index) {
                    ShowOptions -> { changeShowOptionsMemoryRegister.onRead() }
                }
                return memory[index]
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


