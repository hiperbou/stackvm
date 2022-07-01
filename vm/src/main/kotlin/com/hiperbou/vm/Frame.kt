package com.hiperbou.vm

class Frame(val returnAddress: Int) {
    private val variables = mutableMapOf<Int, Int>()

    fun getVariable(varNumber: Int): Int {
        return variables.getOrDefault(varNumber, 0)
    }

    fun setVariable(varNumber: Int, value: Int) {
        variables[varNumber] = value
    }

    fun getVariables(): Map<Int, Int> {
        return variables
    }
}
