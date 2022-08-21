package com.hiperbou.conversation.dsl

import com.hiperbou.conversation.compiler.AsmConversationWriter
import com.hiperbou.conversation.compiler.ConversationWriter
import com.hiperbou.vm.dsl.PROGRAM
import com.hiperbou.vm.dsl.program

class ConversationBuilder(
    val conversationWriter: ConversationWriter = AsmConversationWriter(),
    val conversationState: ConversationState = ConversationState()
): IConversationState by conversationState {

    fun talk(what:String){
        println(what)
        conversationWriter.emitTalk(conversationState.createText(what))
    }

    fun saveMemory(memory: MemoryAddress, value:Int) {
        conversationWriter.emitSaveMemory(memory.address, value)
    }

    fun gotoLabelIfTrue(index:Int, label: Label) {
        conversationWriter.emitGotoLabelIfTrue(index, label.getId())
    }

    fun gotoLabelIfTrue(memory:MemoryAddress, label:Label) {
        conversationWriter.emitGotoLabelIfTrue(memory.address, label.getId())
    }

    fun halt() {
        conversationWriter.emitHalt()
    }

    inner class DialogCharacter(name:String) {
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

        context(ConversationBuilder)
        operator fun invoke(block: ConversationBuilder.() -> Unit) {
            conversationWriter.emitSetCharacter(id)
            block()
        }

        operator fun minus(what:String):DialogCharacter {
            say(what)
            return this
        }

        operator fun plus(what:String):DialogCharacter {
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

        context(ConversationBuilder)
        operator fun invoke(block: ConversationBuilder.(DialogOption) -> Unit) {
            label{}
            block(this)
        }

        init {
            conversationWriter.emitEnableOption(this, enabled)
        }

        override fun toString() = " * $id:$text[$enabled]"
    }

    fun label(label:String, block: ConversationBuilder.() -> Unit) {
        conversationWriter.emitDefineLabel(label)
        block()
    }

    fun label(label:Label, block: ConversationBuilder.() -> Unit) {
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

        val bob = DialogCharacter("Bob")
        val alice = DialogCharacter("Alice")

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

    operator fun String.unaryPlus() {
        talk(this)
    }

    operator fun String.unaryMinus() {
        talk(this)
    }

    fun character(name:String) = DialogCharacter(name)
    fun option(text:String, enabled: Int) = DialogOption(text, enabled)

    fun buildOptions() {
        conversationWriter.emitBuildOptions(conversationState.options)
    }

    fun showOptions() {
        conversationWriter.emitShowOptions()
    }

    fun showOptions(vararg options: DialogOption){
        enableOnly(*options)
        showOptions()
    }

    fun disableAllOptions() {
        conversationState.options.forEach {
            it.disable()
        }
    }

    fun disable(vararg option: DialogOption) {
        option.forEach { it.disable() }
    }

    fun enable(vararg option: DialogOption) {
        option.forEach { it.enable() }
    }

    fun enableOnly(vararg options: DialogOption) {
        conversationState.options.forEach {
            if(it in options) it.enable() else it.disable()
        }
    }

    fun start(block: ConversationBuilder.() -> Unit):IntArray {
        conversationWriter.reset()
        conversationWriter.emitStart()

        block()

        conversationWriter.emitEnd()
        return conversationWriter.compile()
    }

    override fun toString(): String {
        return conversationWriter.toString()
    }
}