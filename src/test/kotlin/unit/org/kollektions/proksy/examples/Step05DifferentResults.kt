package org.kollektions.proksy.examples

import org.junit.Test
import org.kollektions.proksy.output.PrintMockks
import org.kollektions.proksy.recorder.CallRecorder
import org.kollektions.proksy.testmodel.IRover
import org.kollektions.proksy.testmodel.Rover
import java.lang.Exception

class Step05DifferentResults {
    private val sut = Rover()
    private val mockRoverRecorder = CallRecorder.getRecordingProxy<IRover>(sut)
    private val mockRover = mockRoverRecorder.proxy
    private val printer = PrintMockks("MyChangingMockks", mockRoverRecorder.recorder,
        "org.kollektions.proksy.testmodel.IRover",
        "/Users/z002w5y/fun/rekorder/src/test/kotlin/unit",
        "org.kollektions.proksy.examples.mocks")

    @Test
    fun `same arguments, different results`() {
        (0..5).asSequence().forEach {
            try {
                val incrementCount = mockRover.incrementCount()
                println("Iteration: $it, result:$incrementCount")
            } catch (ex: Exception) {
                println("Exception: $ex")
            }
        }
        printer.flushAndPrint("thisRover", "IRover")
    }
}
