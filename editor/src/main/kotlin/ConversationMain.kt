
import com.hiperbou.conversation.device.ConversationDevice
import com.hiperbou.conversation.device.ConversationOptionsDevice
import com.hiperbou.conversation.dsl.*
import com.hiperbou.vm.CPU
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

        private lateinit var cpu:CPU
        private val compiler = LittleCompiler()
        private var pauseCPU = false

        lateinit var memory:Memory

        lateinit var conversation:Conversation

        private fun onStart() {
            startButton.setEnabled(false)

            conversation = Conversation()

            val program2 = conversation.start{
                //0 - set character
                //1 - say
                //2-19 OptionsDevice
                val variableAlreadyTalked = MemoryAddress(32)
                val variableMoreConversation = MemoryAddress(33)

                val labelAlreadyTalked = Label()
                val labelMoreConversation = Label()

                val bob = character("Bob")
                val alice = character("Alice")

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

            val program = conversation.start{
                val bob = character("Bob")
                val alice = character("Alice")

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
            println("updateCharacter: $index ${conversation.getCharacter(index)}")
            labelChar.text = "${conversation.getCharacter(index)}: "
        }

        fun updateText(index: Int){
            labelText.text = conversation.getText(index)
            pauseCPU = true
        }

        private fun onOptionButton(buttonIndex:Int, button:JButton) {
            println("Button pressed $buttonIndex")

            val availableOptions = conversation.getAvailableOptions()
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
            conversation.setOptionEnabled(index, value)
        }

        private lateinit var selectedOption:Conversation.DialogOption
        fun showOptions() {
            println("Show options!")
            val availableOptions = conversation.getAvailableOptions()
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


