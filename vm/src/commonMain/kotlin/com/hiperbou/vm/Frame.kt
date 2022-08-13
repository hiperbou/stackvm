package com.hiperbou.vm

class Frame(val returnAddress: Int = 0) {
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

    override fun toString(): String {
        return """{
            returnAddress: $returnAddress
            variables: $variables
        }""".trimIndent()
    }
}

private fun <K, V> Map<K, V>.getOrDefault(key: K, defaultValue: V): V {
    return getOrElse(key) { defaultValue }
}
