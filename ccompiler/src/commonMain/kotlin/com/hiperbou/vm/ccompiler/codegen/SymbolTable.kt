package com.hiperbou.vm.ccompiler.codegen

class CodeGenException(message: String) : Exception(message)

/**
 * Tracks variable and function symbols during code generation.
 *
 * **Local variables** are stored in the VM's stack frame using `LOAD`/`STORE` with a slot index.
 * Each function has its own scope; slot 0 is the first local variable.
 *
 * **Functions** are tracked by name → bytecode address (resolved after the first pass).
 */
class SymbolTable {

    // -------------------------------------------------------------------------
    // Local variable scope (per function)
    // -------------------------------------------------------------------------

    /** Maps variable name → frame slot index for the current function scope. */
    private val localScopes = mutableListOf<MutableMap<String, Int>>()

    /** Total number of local variable slots allocated in the current function. */
    val localCount: Int
        get() = localScopes.sumOf { it.size }

    /** Enter a new function scope (called at the start of each function). */
    fun enterFunction() {
        localScopes.clear()
        localScopes.add(mutableMapOf())
    }

    /** Exit the current function scope. */
    fun exitFunction() {
        localScopes.clear()
    }

    /** Enter a new block scope (for future block-scoped variables). */
    fun enterBlock() {
        localScopes.add(mutableMapOf())
    }

    /** Exit the current block scope. */
    fun exitBlock() {
        if (localScopes.isNotEmpty()) localScopes.removeAt(localScopes.lastIndex)
    }

    /**
     * Declare a new local variable in the current scope.
     * Returns the assigned frame slot index.
     * Throws [CodeGenException] if the variable is already declared in the current scope.
     */
    fun declareLocal(name: String): Int {
        val currentScope = localScopes.lastOrNull()
            ?: throw CodeGenException("No active scope to declare variable '$name'")
        if (currentScope.containsKey(name)) {
            throw CodeGenException("Variable '$name' is already declared in this scope")
        }
        // Slot index = total number of locals already allocated across all scopes
        val slot = localScopes.sumOf { it.size }
        currentScope[name] = slot
        return slot
    }

    /**
     * Look up a local variable by name.
     * Searches from innermost to outermost scope.
     * Returns the frame slot index, or null if not found.
     */
    fun resolveLocal(name: String): Int? {
        for (scope in localScopes.asReversed()) {
            scope[name]?.let { return it }
        }
        return null
    }

    // -------------------------------------------------------------------------
    // Function registry
    // -------------------------------------------------------------------------

    /** Maps function name → bytecode address. */
    private val functions = mutableMapOf<String, Int>()

    /**
     * Register a function with its bytecode address.
     * Throws [CodeGenException] if already registered.
     */
    fun registerFunction(name: String, address: Int) {
        if (functions.containsKey(name)) {
            throw CodeGenException("Function '$name' is already defined")
        }
        functions[name] = address
    }

    /**
     * Look up a function's bytecode address.
     * Returns null if not yet registered (forward reference — resolved in a second pass).
     */
    fun resolveFunction(name: String): Int? = functions[name]

    /** Returns true if the function has been registered. */
    fun hasFunction(name: String): Boolean = functions.containsKey(name)
}
