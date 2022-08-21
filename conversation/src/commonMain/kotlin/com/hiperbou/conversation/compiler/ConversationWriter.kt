package com.hiperbou.conversation.compiler

import com.hiperbou.conversation.dsl.ConversationBuilder

interface ConversationWriter {
    fun reset()
    fun emitSetCharacter(id: Int)
    fun emitTalk(textIndex: Int)
    fun emitSaveMemory(index: Int, value: Int)
    fun emitDefineLabel(label: String)
    fun emitGotoLabelIfTrue(index: Int, label: String)
    fun emitEnableOption(option: ConversationBuilder.DialogOption, enabled: Int)
    fun emitShowOptions()
    fun emitBuildOptions(options: List<ConversationBuilder.DialogOption>)
    fun emitHalt()
    fun emitStart()
    fun emitEnd()
    fun compile():IntArray
}
