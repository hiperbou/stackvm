package com.hiperbou.conversation.dsl

interface IConversationState {
    fun createCharacter(name: String): Int
    fun createOption(option: ConversationBuilder.DialogOption): Int
    fun createText(text: String): Int
    fun getAvailableOptions(): List<ConversationBuilder.DialogOption>
    fun setOptionEnabled(index: Int, value: Int)
    fun getCharacter(index: Int): String
    fun getText(index: Int): String
}

class ConversationState : IConversationState {
    private val chars = mutableListOf<String>()
    private val texts = mutableListOf<String>()
    val options = mutableListOf<ConversationBuilder.DialogOption>()

    override fun createCharacter(name:String):Int {
        chars.add(name)
        return chars.lastIndex
    }

    override fun createOption(option: ConversationBuilder.DialogOption):Int {
        options.add(option)
        return options.lastIndex
    }

    override fun createText(text:String):Int {
        texts.add(text)
        return texts.lastIndex
    }

    override fun getAvailableOptions() = options.filter { it.enabled != 0 }

    override fun setOptionEnabled(index:Int, value: Int) {
        options[index].enabled = value
    }

    override fun getCharacter(index: Int) = chars[index]
    override fun getText(index: Int) = texts[index]
}
