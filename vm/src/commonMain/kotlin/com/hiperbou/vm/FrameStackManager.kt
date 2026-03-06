package com.hiperbou.vm

class FrameStackManager(
    private val frames: CPUFrames<Frame> = CPUFrames(),
    private val store: ArrayFrameStore = ArrayFrameStore()
) {
    private var localsTop: Int = 0

    init {
        if (frames.isEmpty()) {
            frames.push(createFrame(returnAddress = 0))
        } else {
            normalizeExistingFrames()
        }
    }

    fun currentFrame(): Frame = frames.peek()

    fun pushFrame(returnAddress: Int): Frame {
        val frame = createFrame(returnAddress)
        frames.push(frame)
        return frame
    }

    fun popFrame(instructionAddress: Int): Frame {
        frames.checkThereIsAReturnAddress(instructionAddress)
        val frame = frames.pop()
        if (frame is FrameSlice) {
            localsTop = frame.baseOffset
        }
        return frame
    }

    fun getFrames(): CPUFrames<Frame> = frames

    private fun createFrame(returnAddress: Int): FrameSlice {
        val base = localsTop
        return FrameSlice(returnAddress, store, base) { absoluteIndex ->
            val nextTop = absoluteIndex + 1
            if (nextTop > localsTop) {
                localsTop = nextTop
            }
        }
    }

    private fun normalizeExistingFrames() {
        data class Snapshot(val returnAddress: Int, val variables: Map<Int, Int>)

        val snapshots = mutableListOf<Snapshot>()
        while (frames.isNotEmpty()) {
            val frame = frames.pop()
            snapshots.add(Snapshot(frame.returnAddress, frame.getVariables()))
        }
        snapshots.reverse()

        localsTop = 0
        for (snapshot in snapshots) {
            val frame = createFrame(snapshot.returnAddress)
            for ((slot, value) in snapshot.variables) {
                frame.setVariable(slot, value)
            }
            frames.push(frame)
        }
    }
}
