package com.hiperbou.vm

interface FrameVariableStorage {
    fun get(slot: Int): Int
    fun set(slot: Int, value: Int)
    fun snapshot(): Map<Int, Int>
}

@Deprecated("Map-backed frame storage is migration-only. Prefer array-backed Frame/FrameSlice in runtime code.")
class MapFrameVariableStorage : FrameVariableStorage {
    private val variables = mutableMapOf<Int, Int>()

    override fun get(slot: Int): Int = variables[slot] ?: 0

    override fun set(slot: Int, value: Int) {
        variables[slot] = value
    }

    override fun snapshot(): Map<Int, Int> = variables.toMap()
}

class ArrayFrameStore(initialCapacity: Int = 64) {
    private var data = IntArray(initialCapacity.coerceAtLeast(1))

    fun get(index: Int): Int {
        if (index < 0) throw InvalidProgramException("Negative frame slot index $index")
        return if (index < data.size) data[index] else 0
    }

    fun set(index: Int, value: Int) {
        if (index < 0) throw InvalidProgramException("Negative frame slot index $index")
        ensureCapacity(index)
        data[index] = value
    }

    private fun ensureCapacity(index: Int) {
        if (index < data.size) return
        var newSize = data.size
        while (newSize <= index) newSize *= 2
        data = data.copyOf(newSize)
    }
}

class ArrayFrameVariableStorage(
    private val store: ArrayFrameStore,
    private val baseOffset: Int = 0,
    private val onWriteAbsoluteIndex: ((Int) -> Unit)? = null
) : FrameVariableStorage {
    private val touchedSlots = linkedSetOf<Int>()

    override fun get(slot: Int): Int {
        if (slot < 0) throw InvalidProgramException("Negative frame slot index $slot")
        if (!touchedSlots.contains(slot)) return 0
        return store.get(baseOffset + slot)
    }

    override fun set(slot: Int, value: Int) {
        val absoluteIndex = baseOffset + slot
        store.set(absoluteIndex, value)
        onWriteAbsoluteIndex?.invoke(absoluteIndex)
        touchedSlots.add(slot)
    }

    override fun snapshot(): Map<Int, Int> {
        val out = linkedMapOf<Int, Int>()
        for (slot in touchedSlots) {
            out[slot] = get(slot)
        }
        return out
    }
}

open class Frame(val returnAddress: Int, private val storage: FrameVariableStorage) {
    // Runtime default is now array-backed storage.
    constructor() : this(0, ArrayFrameVariableStorage(ArrayFrameStore()))
    constructor(returnAddress: Int) : this(returnAddress, ArrayFrameVariableStorage(ArrayFrameStore()))

    companion object {
        // Compatibility path for tests/migration scenarios that explicitly need map-backed frames.
        @Suppress("DEPRECATION")
        @Deprecated("Map-backed frames are migration/test-only. Prefer default Frame()/FrameSlice for runtime paths.")
        fun mapBacked(returnAddress: Int = 0): Frame = Frame(returnAddress, MapFrameVariableStorage())
    }

    fun getVariable(varNumber: Int): Int = storage.get(varNumber)

    fun setVariable(varNumber: Int, value: Int) {
        storage.set(varNumber, value)
    }

    fun getVariables(): Map<Int, Int> = storage.snapshot()

    override fun toString(): String {
        return """{
            returnAddress: $returnAddress
            variables: ${getVariables()}
        }""".trimIndent()
    }
}

class FrameSlice(
    returnAddress: Int = 0,
    store: ArrayFrameStore,
    val baseOffset: Int,
    onWriteAbsoluteIndex: ((Int) -> Unit)? = null
) : Frame(returnAddress, ArrayFrameVariableStorage(store, baseOffset, onWriteAbsoluteIndex))
