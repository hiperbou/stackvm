package com.hiperbou.conversation.compiler

import com.hiperbou.conversation.dsl.ConversationBuilder

class AsmConversationWriter(private val compiler:AsmConversationCompiler = DefaultAsmConversationCompiler()):ConversationWriter {

    var buffer = StringBuilder()

    override fun reset() {
        buffer.clear()
    }

    override fun toString() = buffer.toString()

    override fun compile():IntArray {
        return compiler.compile(buffer.toString())
    }

    private fun append(text:String) {
        buffer.append(text).append("\n")
    }

    override fun emitSetCharacter(id:Int) {
        append("""
                    PUSH $id
                    CALL setCharacter 
                """.trimIndent())
    }

    override fun emitTalk(textIndex:Int) {
        append("""
                     PUSH ${textIndex}  
                     CALL say
                """.trimIndent())
    }

    override fun emitSaveMemory(index: Int, value:Int) {
        append("""  
                    PUSH $value
                    WRITE $index
                """.trimIndent())
    }

    override fun emitDefineLabel(label:String) {
        append("""  
                    $label:
                """.trimIndent())
    }

    override fun emitGotoLabelIfTrue(index: Int, label:String) {
        append("""  
                    READ $index
                    JIF $label
                """.trimIndent())
    }

    override fun emitEnableOption(option: ConversationBuilder.DialogOption, enabled: Int) {
        append("""
                    PUSH $enabled
                    WRITE memoryAddressOptions + ${option.id}
                """.trimIndent())
    }

    override fun emitShowOptions() {
        append("""
                    CALL showOptions
                    CALL getSelectedOption
                    JMP optionsSwitch
                """.trimIndent())
    }

    override fun emitBuildOptions(options:List<ConversationBuilder.DialogOption>) {
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

    override fun emitHalt() {
        append("""  
                    HALT
                """.trimIndent())
    }

    override fun emitStart() {
        append("""
                    
                    memoryAddressSetCharacter: 0
                    memoryAddressSay: 1
                    
                    memoryAddressOptions: 2
                    memoryAddressShowOptions: memoryAddressOptions + 16
                    memoryAddressSelectedOption: memoryAddressShowOptions + 1
                    
                """.trimIndent())
    }

    override fun emitEnd() {
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
                    READ memoryAddressSelectedOption
                    RET
                
                """.trimIndent())
    }
}