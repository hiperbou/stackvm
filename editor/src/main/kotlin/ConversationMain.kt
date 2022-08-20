
import com.hiperbou.conversation.ConversationCPU
import com.hiperbou.conversation.compiler.AsmConversationWriter
import com.hiperbou.conversation.controller.ConversationOptionsController
import com.hiperbou.conversation.controller.ConversationTalkController
import com.hiperbou.conversation.dsl.*
import com.xemantic.kotlin.swing.mainFrame
import com.xemantic.kotlin.swing.verticalPanel
import java.awt.Color
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


class ConversationMain {
    class ConversationDemo: ConversationOptionsController, ConversationTalkController {

        lateinit var labelChar:JLabel
        lateinit var labelText:JLabel
        lateinit var startButton:JButton
        lateinit var nextButton:JButton

        private val optionButtons = mutableListOf<JButton>()

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

        private lateinit var conversationState:IConversationState

        private val conversationCPU = ConversationCPU(conversationTalkController = this, conversationOptionsController = this)
        private val asmConversationWriter = AsmConversationWriter(LittleCompiler())

        data class CompiledConversation(val state:IConversationState, val program:IntArray)

        fun conversation(block: ConversationBuilder.() -> Unit):CompiledConversation {
            val conversationBuilder = ConversationBuilder(asmConversationWriter)
            val program = conversationBuilder.start { block() }
            println(conversationBuilder)
            return CompiledConversation(conversationBuilder.conversationState, program)
        }

        private fun onStart() {
            startButton.setEnabled(false)

            val program2 = conversation {
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

            val program = conversation {
                val bob = character("Bob")
                val alice = character("Alice")

                val labelAlreadyTalked = Label()
                val variableAlreadyTalked = MemoryAddress(32)
                labelAlreadyTalked.gotoIfTrue(variableAlreadyTalked)

                val optionSaludar = option("hola", 1)
                val optionRobar = option("dame el oro", 1)
                val optionRobarConViolencia = option("dame el oro ahora", 0)
                val optionHidden = option("I'm sorry for being rude", 0)
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
                    variableAlreadyTalked.set(true)
                    halt()
                }

                optionHidden {
                    bob - "I'm sorry for being rude"
                    alice - "That's ok."
                    alice - "Now go away."
                    variableAlreadyTalked.set(false)
                    halt()
                }

                labelAlreadyTalked {
                    bob - "Hey..."
                    alice - "We have nothing else to discuss"

                    showOptions(optionHidden, optionAdios)
                }
            }

            val randomConversation = listOf(program, program2).random()
            conversationState = randomConversation.state
            conversationCPU.reset(randomConversation.program)
            onNext()
        }

        private fun onNext() {
            conversationCPU.run()
            refreshUI()
        }

        private fun refreshUI() {
            if (conversationCPU.isHalted()) {
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

        private fun onOptionButton(buttonIndex:Int, button:JButton) {
            println("Button pressed $buttonIndex")

            val availableOptions = conversationState.getAvailableOptions()
            selectedOption = availableOptions[buttonIndex]
            disableOptionButons()
            onNext()
        }

        private fun disableOptionButons(){
            optionButtons.forEach {
                it.setEnabled(false)
            }
        }

        override fun updateCharacter(index: Int) {
            changeTextColor(when(index){
                0 -> Color.BLUE
                else -> Color.RED
            })
            println("updateCharacter: $index ${conversationState.getCharacter(index)}")
            labelChar.text = "${conversationState.getCharacter(index)}: "
        }

        override fun updateText(index: Int){
            labelText.text = conversationState.getText(index)
            conversationCPU.pause()
        }

        override fun updateOption(index:Int, value:Int) {
            println("Updating option $index to $value")
            conversationState.setOptionEnabled(index, value)
        }

        private lateinit var selectedOption:ConversationBuilder.DialogOption
        override fun showOptions() {
            println("Show options!")
            val availableOptions = conversationState.getAvailableOptions()
            availableOptions.forEach { println(it) }
            conversationCPU.pause()

            availableOptions.forEachIndexed { index, it ->
                optionButtons[index].apply {
                    text = it.text
                    setEnabled(it.enabled == 1)
                }
            }
            selectedOption = availableOptions.random()
        }

        override fun getSelectedOption():Int {
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


