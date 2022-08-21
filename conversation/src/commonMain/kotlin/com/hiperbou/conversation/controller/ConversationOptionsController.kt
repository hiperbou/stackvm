package com.hiperbou.conversation.controller

interface ConversationOptionsController {
    fun showOptions()
    fun getSelectedOption():Int
    fun updateOption(index:Int, value:Int)
}