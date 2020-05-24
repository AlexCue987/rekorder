package org.kollektions.proksy.examples

import org.kollektions.proksy.*
import org.kollektions.proksy.output.PrintMockks
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class Step01RecordOneAtATime {
    private val sut = Rover()
    private val mockRoverRecorder = CallRecorder.getRecordingProxy<IRover>(sut)
    private val mockRover = mockRoverRecorder.proxy
    private val printer = PrintMockks("MyMockks", mockRoverRecorder.recorder)

    @Test
    fun test1() {
        val actual = mockRover.explore(1)
        assertEquals(2, actual)
        printer.flushAndPrint("thisRover1", "IRover")
    }

    @Test
    fun test2() {
        val actual = mockRover.add(BigDecimal.ONE, LocalDate.MIN, "Any", 1)
        assertEquals(2, actual)
        printer.flushAndPrint("thisRover2", "IRover")
    }
}
