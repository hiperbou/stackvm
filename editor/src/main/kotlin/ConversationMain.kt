
import com.hiperbou.conversation.compiler.AsmConversationWriter
import com.hiperbou.conversation.compiler.ConversationWriter
import com.hiperbou.conversation.device.ConversationDevice
import com.hiperbou.conversation.device.ConversationOptionsDevice
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

        class ConversationState {
            val chars = mutableListOf<String>()
            val texts = mutableListOf<String>()
            val options = mutableListOf<Conversation.DialogOption>()

            fun createCharacter(name:String):Int {
                chars.add(name)
                return chars.lastIndex
            }

            fun createOption(option: Conversation.DialogOption):Int {
                options.add(option)
                return options.lastIndex
            }

            fun createText(text:String):Int {
                texts.add(text)
                return texts.lastIndex
            }
        }



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


        inner class Conversation(
            val conversationWriter:AsmConversationWriter = AsmConversationWriter(),
            val conversationState:ConversationState = ConversationState()
        ) {
            init {
                //conversationState.clear() //just build a new one
                //chars.clear()
                //texts.clear()
                //options.clear()
            }
            fun createCharacter(name:String):Int {
                return conversationState.createCharacter(name)
            }

            fun createOption(option:DialogOption):Int {
                return conversationState.createOption(option)
            }

            fun PROGRAM.characterBin(id:Int, setCharacterAddress:Int = 2) {
                write(
                    PUSH, id,
                    CALL, setCharacterAddress
                )
            }

            fun talk(what:String){
                println(what)
                conversationWriter.emitTalk(conversationState.createText(what))
            }

            fun PROGRAM.talkBin(what:String, sayAddress:Int = 5) {
                write(
                    PUSH, conversationState.createText(what),
                    CALL, sayAddress
                )
            }

            fun saveMemory(memory: MemoryAddress, value:Int) {
                conversationWriter.emitSaveMemory(memory.address, value)
            }

            fun PROGRAM.saveMemoryBin(index: Int, value:Int) {
                write(
                    PUSH, value,
                    WRITE, index
                )
            }


            fun gotoLabelIfTrue(index:Int, label:Label) {
                conversationWriter.emitGotoLabelIfTrue(index, label.getId())
            }

            fun gotoLabelIfTrue(memory:MemoryAddress, label:Label) {
                conversationWriter.emitGotoLabelIfTrue(memory.address, label.getId())
            }

            fun halt() {
                conversationWriter.emitHalt()
            }

            fun PROGRAM.haltBin() {
                write(
                    HALT
                )
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
                    conversationWriter.emitSetCharacter(id)
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
                    conversationWriter.emitSetCharacter(id)
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
                    conversationWriter.emitSetCharacter(id)
                }
            }

            inner class DialogOption(val text:String, var enabled:Int) {
                val id = createOption(this)

                val label = Label()//("option_${text.replace(" ","_")}")

                fun enable() = conversationWriter.emitEnableOption(this, 1)
                fun disable() = conversationWriter.emitEnableOption(this, 0)

                context(Conversation)
                operator fun invoke(block: ConversationDemo.Conversation.(DialogOption) -> Unit) {
                    label{}
                    block(this)
                }

                init {
                    conversationWriter.emitEnableOption(this, enabled)
                }

                override fun toString() = " * $id:$text[$enabled]"
            }

            fun label(label:String, block: ConversationDemo.Conversation.() -> Unit) {
                conversationWriter.emitDefineLabel(label)
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
                conversationWriter.reset()
                conversationWriter.emitStart()

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
                    conversationWriter.emitBuildOptions(conversationState.options)
                }

                fun showOptions(){
                    conversationWriter.emitShowOptions()
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

                conversationWriter.emitEnd()
                return conversationWriter.toString()
            }
        }

        private lateinit var cpu:CPU
        private val compiler = LittleCompiler()
        private var pauseCPU = false

        lateinit var memory:Memory

        var conv = Conversation() //TODO: improve this
        var conversationState = conv.conversationState//TODO: improve this

        private fun onStart() {
            startButton.setEnabled(false)

            conv = Conversation()//TODO: improve this
            conversationState = conv.conversationState//TODO: improve this

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
            println("updateCharacter: $index ${conversationState.chars[index]}")
            labelChar.text = "${conversationState.chars[index]}: "
        }

        fun updateText(index: Int){
            labelText.text = conversationState.texts[index]
            pauseCPU = true
        }

        private fun onOptionButton(buttonIndex:Int, button:JButton) {
            println("Button pressed $buttonIndex")

            val availableOptions = conversationState.options.filter { it.enabled != 0 }
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
            conversationState.options[index].enabled = value
        }

        private lateinit var selectedOption:Conversation.DialogOption
        fun showOptions() {
            println("Show options!")
            val availableOptions = conversationState.options.filter { it.enabled != 0 }
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

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) = mainFrame("conversationVM") {
            contentPane = ConversationDemo().app()
        }
    }
}


