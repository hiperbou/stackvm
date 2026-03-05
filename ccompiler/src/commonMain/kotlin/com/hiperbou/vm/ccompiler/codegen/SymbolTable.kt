package com.hiperbou.vm.ccompiler.codegen

class CodeGenException(message: String) : Exception(message)

/**
 * Tracks variable and function symbols during code generation.
 */
class SymbolTable {

    // -------------------------------------------------------------------------
    // Global variables
    // -------------------------------------------------------------------------

    private val globals = mutableMapOf<String, Int>()

    fun declareGlobal(name: String): Int {
        if (globals.containsKey(name)) {
            throw CodeGenException("Global variable '$name' is already declared")
        }
        val slot = globals.size
        globals[name] = slot
        return slot
    }

    fun resolveGlobal(name: String): Int? = globals[name]

    // -------------------------------------------------------------------------
    // Local variable scope (per function)
    // -------------------------------------------------------------------------

    private val localScopes = mutableListOf<MutableMap<String, Int>>()

    val localCount: Int
        get() = localScopes.sumOf { it.size }

    fun enterFunction() {
        localScopes.clear()
        localScopes.add(mutableMapOf())
    }

    fun exitFunction() {
        localScopes.clear()
    }

    fun enterBlock() {
        localScopes.add(mutableMapOf())
    }

    fun exitBlock() {
        if (localScopes.isNotEmpty()) localScopes.removeAt(localScopes.lastIndex)
    }

    fun declareLocal(name: String): Int {
        val currentScope = localScopes.lastOrNull()
            ?: throw CodeGenException("No active scope to declare variable '$name'")
        if (currentScope.containsKey(name)) {
            throw CodeGenException("Variable '$name' is already declared in this scope")
        }
        val slot = localScopes.sumOf { it.size }
        currentScope[name] = slot
        return slot
    }

    fun resolveLocal(name: String): Int? {
        for (scope in localScopes.asReversed()) {
            scope[name]?.let { return it }
        }
        return null
    }

    // -------------------------------------------------------------------------
    // Function registry
    // -------------------------------------------------------------------------

    private val functions = mutableMapOf<String, Int>()

    fun registerFunction(name: String, address: Int) {
        if (functions.containsKey(name)) {
            throw CodeGenException("Function '$name' is already defined")
        }
        functions[name] = address
    }

    fun resolveFunction(name: String): Int? = functions[name]

    fun hasFunction(name: String): Boolean = functions.containsKey(name)
}
