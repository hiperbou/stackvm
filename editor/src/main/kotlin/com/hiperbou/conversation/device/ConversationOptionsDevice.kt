package com.hiperbou.conversation.device

import com.hiperbou.vm.memory.DeviceMapper
import com.hiperbou.vm.memory.Memory
import com.hiperbou.vm.memory.ReadWriteMemoryRegister
import com.hiperbou.vm.memory.WriteMemoryRegister

class ConversationOptionsDevice(private val memory: Memory, val conversationDemo: ConversationMain.ConversationDemo): Memory by memory {
    companion object {
        private const val maxOptions = 16
        private const val optionSize = 1

        const val ShowOptions = maxOptions * optionSize
        const val SelectedOption = ShowOptions + 1

        const val size = SelectedOption + 1

        fun builder(conversationDemo: ConversationMain.ConversationDemo) = DeviceMapper({ size }) { ConversationOptionsDevice(it, conversationDemo) }
    }

    override fun size() = size


    private inner class ChangeShowOptionsMemoryRegister(private val memory: Memory): WriteMemoryRegister {
        override fun onWrite(value: Int) {
            println("VM: Showing dialogue options")
            memory[ShowOptions] = value // we don't care about the value at all
            conversationDemo.showOptions()
        }
    }

    private inner class ChangeSelectedOptionMemoryRegister(private val memory: Memory): ReadWriteMemoryRegister {
        override fun onWrite(value: Int) {
            println("VM: Changing selected option to $value")
            memory[SelectedOption] = value
        }

        override fun onRead() {
            println("VM: reading selected dialogue option")
            memory[SelectedOption] = conversationDemo.getSelectedOption()
        }
    }

    private val changeShowOptionsMemoryRegister = ChangeShowOptionsMemoryRegister(memory)
    private val changeSelectedOptionMemoryRegister = ChangeSelectedOptionMemoryRegister(memory)

    override operator fun set(index: Int, value:Int) {
        when(index) {
            ShowOptions -> changeShowOptionsMemoryRegister.onWrite(value)
            SelectedOption -> changeSelectedOptionMemoryRegister.onWrite(value)
            else -> {
                println("VM: Changing option $index to $value")
                memory[index] = value
                conversationDemo.updateOption(index, value)
            }
        }
    }

    override fun get(index: Int): Int {
        when(index) {
            SelectedOption -> { changeSelectedOptionMemoryRegister.onRead() }
        }
        return memory[index]
    }
}