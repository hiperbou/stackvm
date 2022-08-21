package com.hiperbou.conversation.device

import com.hiperbou.conversation.controller.ConversationTalkController
import com.hiperbou.vm.memory.DeviceMapper
import com.hiperbou.vm.memory.Memory
import com.hiperbou.vm.memory.WriteMemoryRegister


class ConversationDevice(private val memory: Memory, val conversationTalkController: ConversationTalkController): Memory by memory {
    companion object {
        const val Character = 0
        const val Text = 1

        const val size = 2

        fun builder(conversationTalkController: ConversationTalkController) = DeviceMapper({ size }) { ConversationDevice(it, conversationTalkController) }
    }

    override fun size() = size

    private inner class ChangeCharacterMemoryRegister(private val memory: Memory): WriteMemoryRegister {
        override fun onWrite(value: Int) {
            println("VM: Changing to character $value")
            memory[Character] = value
            conversationTalkController.updateCharacter(value)
        }
    }

    private inner class ChangeTextMemoryRegister(private val memory: Memory): WriteMemoryRegister {
        override fun onWrite(value: Int) {
            println("VM: Changing text to $value")
            memory[Text] = value
            conversationTalkController.updateText(value)
        }
    }

    private val changeCharacterMemoryRegister = ChangeCharacterMemoryRegister(memory)
    private val changeTextMemoryRegister = ChangeTextMemoryRegister(memory)

    override operator fun set(index: Int, value:Int) {
        when(index) {
            Character -> changeCharacterMemoryRegister.onWrite(value)
            Text -> changeTextMemoryRegister.onWrite(value)
            else -> {}
        }
    }
}