package com.hiperbou.conversation

import com.hiperbou.conversation.controller.ConversationOptionsController
import com.hiperbou.conversation.controller.ConversationTalkController
import com.hiperbou.conversation.device.ConversationDevice
import com.hiperbou.conversation.device.ConversationOptionsDevice
import com.hiperbou.vm.CPU
import com.hiperbou.vm.memory.Memory
import com.hiperbou.vm.memory.MemoryMapper

class ConversationCPU(
    instructions:IntArray,
    conversationTalkController: ConversationTalkController,
    conversationOptionsController: ConversationOptionsController,
    memory: Memory = getMapper(conversationTalkController, conversationOptionsController)
) {
    private val cpu = CPU(instructions, memory = memory)

    private var pauseCPU = false

    fun reset() = cpu.reset()
    fun isHalted() = cpu.isHalted()
    fun pause() { pauseCPU = true }

    fun run() {
        pauseCPU = false
        while(!cpu.isHalted() && !pauseCPU) {
            cpu.step()
        }
    }

    companion object{
        fun getMapper(conversationTalkController: ConversationTalkController,
                      conversationOptionsController: ConversationOptionsController): MemoryMapper {
            return MemoryMapper(IntArray(UByte.MAX_VALUE.toInt())).apply {
                map(ConversationDevice.builder(conversationTalkController))
                map(ConversationOptionsDevice.builder(conversationOptionsController))
            }
        }
    }
}