package com.hiperbou.conversation.compiler

import com.hiperbou.conversation.dsl.ConversationBuilder
import com.hiperbou.vm.Instructions

class BinConversationWriter:ConversationWriter {
    private val setCharacterAddress:Int = 2
    private val sayAddress:Int = 5

    private val program = mutableListOf<Int>()

    fun write(vararg values: Int) {
        program.addAll(values.toTypedArray())
    }

    fun overwriteWithCurrentSize(index:Int) {
        program[index] = program.size
    }

    override fun compile(): IntArray {
        return program.toIntArray()
    }

    override fun reset() {
        program.clear()
    }

    override fun emitSetCharacter(id:Int) {
        write(
            Instructions.PUSH, id,
            Instructions.CALL, setCharacterAddress
        )
    }

    override fun emitTalk(textIndex:Int) {
        write(
            Instructions.PUSH, textIndex,
            Instructions.CALL, sayAddress
        )
    }

    override fun emitSaveMemory(index: Int, value: Int) {
        write(
            Instructions.PUSH, value,
            Instructions.WRITE, index
        )
    }

    override fun emitHalt() {
        write(
            Instructions.HALT
        )
    }

    override fun emitDefineLabel(label: String) {
        TODO("Not yet implemented")
    }

    override fun emitGotoLabel(label: String) {
        TODO("Not yet implemented")
    }

    override fun emitGotoLabelIfTrue(index: Int, label: String) {
        TODO("Not yet implemented")
    }

    override fun emitEnableOption(option: ConversationBuilder.DialogOption, enabled: Int) {
        TODO("Not yet implemented")
    }

    override fun emitShowOptions() {
        TODO("Not yet implemented")
    }

    override fun emitBuildOptions(options: List<ConversationBuilder.DialogOption>) {
        TODO("Not yet implemented")
    }

    override fun emitStart() {
        write(
            Instructions.JMP, Instructions.NOP, //JUMP TO CODE START
            //setCharacter:
            Instructions.WRITE, 0,
            Instructions.RET,
            //say:
            Instructions.WRITE, 1,
            Instructions.RET
        )
        overwriteWithCurrentSize(1)
    }

    override fun emitEnd() {
        write(
            Instructions.HALT
        )
    }

}