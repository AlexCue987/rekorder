package org.kollektions.proksy

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class Step01RecordOneAtATime {
    private val sut = Rover()
    private val recorder = CallRecorder()
    private val mockRover = CallRecorder.getProxy<IRover>(sut, recorder)

    @Test
    fun test1() {
        val actual = mockRover.explore(1)
        assertEquals(2, actual)
        flushAndPrint("ThisRover1")
    }

    private fun flushAndPrint(mockName: String) {
        val calls = recorder.flush()
        val callSummaries = CallsOrganizer().organize(calls)
        val generator = mocksGenerator("rover", "Rover")
        val mocks = generator.generateCallsOfEvery(mockName, callSummaries)
        println(mocks)
    }

    @Test
    fun test2() {
        val actual = mockRover.add(BigDecimal.ONE, LocalDate.MIN, "Any", 1)
        assertEquals(2, actual)
        flushAndPrint("ThisRover2")
    }

}
