package com.hiperbou.vm.ccompiler.codegen

class CodeGenException(message: String) : Exception(message)

/**
 * Tracks variable and function symbols during code generation.
 */
class SymbolTable {

    data class VariableSymbol(val slot: Int, val size: Int = 1)

    // -------------------------------------------------------------------------
    // Global variables
    // -------------------------------------------------------------------------

    private val globals = mutableMapOf<String, VariableSymbol>()
    private var globalNextSlot: Int = 0

    fun declareGlobal(name: String, size: Int = 1): Int {
        if (globals.containsKey(name)) {
            throw CodeGenException("Global variable '$name' is already declared")
        }
        if (size <= 0) throw CodeGenException("Invalid global size $size for '$name'")
        val slot = globalNextSlot
        globals[name] = VariableSymbol(slot, size)
        globalNextSlot += size
        return slot
    }

    fun resolveGlobal(name: String): Int? = globals[name]?.slot
    fun resolveGlobalSymbol(name: String): VariableSymbol? = globals[name]

    // -------------------------------------------------------------------------
    // Local variable scope (per function)
    // -------------------------------------------------------------------------

    private val localScopes = mutableListOf<MutableMap<String, VariableSymbol>>()
    private var localNextSlot: Int = 0

    val localCount: Int
        get() = localNextSlot

    fun enterFunction() {
        localScopes.clear()
        localScopes.add(mutableMapOf())
        localNextSlot = 0
    }

    fun exitFunction() {
        localScopes.clear()
        localNextSlot = 0
    }

    fun enterBlock() {
        localScopes.add(mutableMapOf())
    }

    fun exitBlock() {
        if (localScopes.isNotEmpty()) localScopes.removeAt(localScopes.lastIndex)
    }

    fun declareLocal(name: String, size: Int = 1): Int {
        val currentScope = localScopes.lastOrNull()
            ?: throw CodeGenException("No active scope to declare variable '$name'")
        if (currentScope.containsKey(name)) {
            throw CodeGenException("Variable '$name' is already declared in this scope")
        }
        if (size <= 0) throw CodeGenException("Invalid local size $size for '$name'")
        val slot = localNextSlot
        currentScope[name] = VariableSymbol(slot, size)
        localNextSlot += size
        return slot
    }

    fun resolveLocal(name: String): Int? = resolveLocalSymbol(name)?.slot

    fun resolveLocalSymbol(name: String): VariableSymbol? {
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
