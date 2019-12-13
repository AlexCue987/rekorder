package org.kollektions.proksy

import org.kollektions.proksy.CallRecorder.Companion.getProxy
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CallRecorderTest {
    val sut = CallRecorder()
    val instanceToMock = Rover()
    val proxy = getProxy<IRover>(instanceToMock, sut)

    @Test
    fun `record result and return it unchanged`() {
        val returnedResult = proxy.explore(1)
        val results = sut.getCalls()
        val expected = FunctionCall("explore", listOf(1), ObjectResult(returnedResult))
        assertEquals(listOf(expected), results)
    }

    @Test
    fun `record no results and return Unit`() {
        val returnedResult = proxy.doNothing(1)
        val results = sut.getCalls()
        val expected = FunctionCall("doNothing", listOf(1), UnitResult())
        assertEquals(listOf(expected), results)
        assertTrue { returnedResult is Unit }
    }

    @Test
    fun `record and rethrow exception`() {
        val reason = "Just because I said so"
        val exception = assertFailsWith<Exception> { proxy.validateReason(reason) }
        val results = sut.getCalls()
        val expected = FunctionCall("validateReason", listOf(reason), ExceptionResult(Exception("Bad reason: \"Just because I said so\"")))
        assertEquals(listOf(expected), results)
    }

}

interface IRover {
    fun explore(a: Int): Int
    fun add(x: BigDecimal, d: LocalDate, s: String, l: Int): Int
    fun expand(l: Int, t: List<Any>): Int
    fun doSomething(times: Int, what: Something): Something
    fun doNothing(times: Int)
    fun validateReason(reason: String): String
}

class Rover() : IRover {
    override fun doNothing(times: Int) {}

    override fun explore(a: Int): Int {
        println("Exploring $a")
        return a+1
    }

    override fun add(x: BigDecimal, d: LocalDate, s: String, l: Int) = l+1

    override fun expand(l: Int, t: List<Any>) = l + t.size

    override fun doSomething(times: Int, what: Something) = what

    override fun validateReason(reason: String): String = if(reason == "Because") "Correct" else throw Exception("Bad reason: \"$reason\"")
}

data class Something(val name: String, val color: String, val weight: BigDecimal, val tags: List<String>)
