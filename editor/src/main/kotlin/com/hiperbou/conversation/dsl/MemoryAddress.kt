package com.hiperbou.conversation.dsl

class MemoryAddress(val address:Int = -1) {

    context(ConversationBuilder)
    fun set(value: Int):MemoryAddress {
        saveMemory(this, value)
        return this
    }

    context(ConversationBuilder)
    fun set(boolean: Boolean):MemoryAddress {
        return set(if (boolean) 1 else 0)
    }

    //context(ConversationMain.ConversationDemo.Conversation)
    //operator fun invoke(block: ConversationMain.ConversationDemo.Conversation.() -> Unit) {
        //label(this, block)
    //}

}