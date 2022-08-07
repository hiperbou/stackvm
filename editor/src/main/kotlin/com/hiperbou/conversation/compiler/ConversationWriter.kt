package com.hiperbou.conversation.compiler


interface ConversationWriter

class AsmConversationWriter(/*var program:String = ""*/):ConversationWriter {

    var buffer = StringBuilder()

    fun reset() {
        buffer.clear()
    }

    override fun toString() = buffer.toString()

    private fun append(text:String) {
        //program = program + "\n" + text
        buffer.append(text).append("\n")
    }

    fun emitSetCharacter(id:Int) {
        append("""
                    PUSH $id
                    CALL setCharacter 
                """.trimIndent())
    }

    fun emitTalk(textIndex:Int) {
        append("""
                     PUSH ${textIndex}  
                     CALL say
                """.trimIndent())
    }

    fun emitSaveMemory(index: Int, value:Int) {
        append("""  
                    PUSH $value
                    WRITE $index
                """.trimIndent())
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

    fun emitEnableOption(option: ConversationMain.ConversationDemo.Conversation.DialogOption, enabled: Int) {
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

    fun emitBuildOptions(options:List<ConversationMain.ConversationDemo.Conversation.DialogOption>) {
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

    fun emitHalt() {
        append("""  
                    HALT
                """.trimIndent())
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
                    READ memoryAddressSelectedOption
                    RET
                
                """.trimIndent())
    }
}