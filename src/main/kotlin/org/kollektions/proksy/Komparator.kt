package org.kollektions.proksy

//class SourceAggregator<T>(val numSequences: Int, val consumer: (value: T) -> Unit) {
//    private val items = (1..numSequences).asSequence().map { null as T? }.toMutableList()
//    private val states = (1..numSequences).asSequence().map { SourceState.Pending }.toMutableList()
//    private val sequences =
//        (1..numSequences).asSequence().map { OrderedSequence<T>() }.toMutableList()
//
//    @Synchronized
//    fun setNextItem(value: T, seqNum: Int) {
//        sequences[seqNum].setNextItem(value)
//    }
//
//    @Synchronized
//    fun close(seqNum: Int) {
//        sequences[seqNum].close()
//    }
//
//    fun processEvent() {
//        if ()
//    }
//
//}

interface OrderedSource<T> {
    fun nextValue(): T
    fun close()
    val state: SourceState
}

enum class SourceState{Pending, Open, Closed}

class OrderedSequence<T>(): OrderedSource<T> {
    private var sourceState: SourceState = SourceState.Pending
    private var nextItem: T? = null

    override fun nextValue(): T {
        return when(sourceState) {
            SourceState.Open -> nextItem!!
            else -> throw IllegalStateException("Cannot return next value in state: $sourceState")
        }
    }

    fun setNextItem(value: T) {
        if(sourceState == SourceState.Closed) {
            throw IllegalStateException("Cannot set next value in state: $sourceState")
        }
        sourceState = SourceState.Open
        nextItem = value
    }

    override fun close() {
        sourceState = SourceState.Closed
    }

    override val state: SourceState
        get() = sourceState
}
