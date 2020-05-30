package org.kollektions.proksy.generator

import org.kollektions.proksy.model.FunctionCall
import org.kollektions.proksy.model.ObjectResult
import kotlin.test.Test

class CallsOrganizerTest {
    private val calls = listOf(
        FunctionCall("start", listOf(1, "Any"), ObjectResult("OK")),
        FunctionCall("stop", listOf(), ObjectResult(42)),
        FunctionCall("start", listOf(1, "Any"), ObjectResult("OK")),
        FunctionCall("stop", listOf(), ObjectResult(43)),
        FunctionCall("start", listOf(1, "All"), ObjectResult("Not OK")),
        FunctionCall("stop", listOf(), ObjectResult(43))
    )

    private val sut = CallsOrganizer()

    @Test
    fun `organize works`() {
        val actual = sut.organize(calls)
        actual.forEach { println(it) }
    }
}
