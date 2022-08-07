package com.hiperbou.conversation.dsl

class Label(var _id:String = "") {
    fun getId():String {
        if (_id.isEmpty()){
            _id = "label_${labelID++}"
        }
        return _id
    }

    context(Conversation)
            operator fun invoke(block: Conversation.() -> Unit) {
        label(this, block)
    }

    context(Conversation)
    fun gotoIfTrue(memory: MemoryAddress) {
        gotoLabelIfTrue(memory, this)
    }

    companion object{
        var labelID = 0
    }
}