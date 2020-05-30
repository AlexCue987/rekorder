package org.kollektions.proksy.examples

import org.junit.AfterClass
import org.kollektions.proksy.output.PrintMockks
import org.kollektions.proksy.recorder.CallRecorder
import org.kollektions.proksy.testmodel.IRover
import org.kollektions.proksy.testmodel.Rover
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class Step01RecordAllAtOnce {
    @Test
    fun test1() {
        val actual = mockRover.explore(1)
        assertEquals(2, actual)
    }

    @Test
    fun test2() {
        val actual = mockRover.add(BigDecimal.ONE, LocalDate.MIN, "Any", 1)
        assertEquals(2, actual)
    }

    companion object {
        private val sut = Rover()
        private val mockRoverRecorder = CallRecorder.getRecordingProxy<IRover>(sut)
        private val mockRover = mockRoverRecorder.proxy
        private val printer = PrintMockks("MyMockks2",
            mockRoverRecorder.recorder,
            "org.kollektions.proksy.testmodel.IRover",
        "/Users/z002w5y/fun/rekorder/src/test/kotlin/unit",
        "org.kollektions.proksy.examples.mocks")

        @JvmStatic
        @AfterClass
        fun saveMocks() {
            printer.flushAndPrint("thisRover", "IRover")
        }
    }
}
