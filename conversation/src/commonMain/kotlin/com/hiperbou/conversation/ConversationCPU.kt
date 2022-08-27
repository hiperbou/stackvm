package com.hiperbou.conversation

import com.hiperbou.conversation.controller.ConversationOptionsController
import com.hiperbou.conversation.controller.ConversationTalkController
import com.hiperbou.conversation.device.ConversationDevice
import com.hiperbou.conversation.device.ConversationOptionsDevice
import com.hiperbou.vm.CPU
import com.hiperbou.vm.Instructions
import com.hiperbou.vm.memory.Memory
import com.hiperbou.vm.memory.MemoryMapper

class ConversationCPU(
    memoryMapper: MemoryMapper,
    instructions:IntArray = IntArray(Instructions.HALT)
) {
    constructor(
        conversationTalkController: ConversationTalkController,
        conversationOptionsController: ConversationOptionsController
    ):this(getMapper(conversationTalkController, conversationOptionsController))

    private val cpu = CPU(instructions, memory = memoryMapper)

    private var pauseCPU = false

    fun reset(instructions:IntArray?) = cpu.reset(instructions)
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
                      conversationOptionsController: ConversationOptionsController,
                      memoryArray:IntArray = IntArray(UByte.MAX_VALUE.toInt())
                      ): MemoryMapper {
            return MemoryMapper(memoryArray).apply {
                map(ConversationDevice.builder(conversationTalkController))
                map(ConversationOptionsDevice.builder(conversationOptionsController))
            }
        }
    }
}