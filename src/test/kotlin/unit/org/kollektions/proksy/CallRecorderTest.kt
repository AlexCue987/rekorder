package org.kollektions.proksy

import io.mockk.every
import io.mockk.mockk
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
        assertEquals(listOf(expected), results, "Result recorded")
        assertEquals(2, returnedResult, "proxy returns same result as rover")
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
        val reason = "Because I said so"
        val exception = assertFailsWith<TestException> { proxy.validateReason(reason) }
        val results = sut.getCalls()
        val expected = FunctionCall("validateReason", listOf(reason), ExceptionResult(TestException("Bad reason: \"Because I said so\"")))
        assertEquals(listOf(expected), results)
    }

    @Test
    fun myTest() {
        println("--------- MyTest -------------")
        val rover = mockk<Rover>()
        (every { rover.explore(1) }
            returns 1
            andThenThrows TestException("Oops")
            andThen 3)
        (1..4).asSequence().forEach {
            println("--------- $it -------------")
            try {
                println("Ran ${rover.explore(1)}")
            } catch (ex: Exception) {
                println("Caught: $ex")
            }
        }
    }

    @Test
    fun myTest2() {
        println("--------- MyTest -------------")
        val rover = mockk<Rover>()
        (every { rover.explore(1) }
            returnsMany listOf(1, 2, 3))
        (1..6).asSequence().forEach {
            println("--------- $it -------------")
            try {
                println("Ran ${rover.explore(1)}")
            } catch (ex: Exception) {
                println("Caught: $ex")
            }
        }
    }

    @Test
    fun increment() {
        (1..5).forEach {
            try {
                println("Ran ${instanceToMock.incrementCount()}")
            } catch (ex: Exception) {
                println("Caught: $ex")
            }
        }
    }

    @Test
    fun `Elvis example`() {
        val nullNumber: BigDecimal? = null
        val notNullNumber: BigDecimal? = BigDecimal.ONE
        println((nullNumber ?: "null").toString())
        println((notNullNumber ?: "null").toString())
    }

}

interface IRover {
    fun explore(a: Int): Int
    fun add(x: BigDecimal, d: LocalDate, s: String, l: Int): Int
    fun expand(l: Int, t: List<Any>): Int
    fun doSomething(times: Int, what: Something): Something
    fun doNothing(times: Int)
    fun validateReason(reason: String): String
    fun incrementCount(): Int
}

class Rover() : IRover {
    var count = 0

    override fun doNothing(times: Int) {}

    override fun explore(a: Int): Int {
        println("Exploring $a")
        return a+1
    }

    override fun incrementCount(): Int {
        return if (++count == 2) throw TestException("Not 2") else count
    }

    override fun add(x: BigDecimal, d: LocalDate, s: String, l: Int) = l+1

    override fun expand(l: Int, t: List<Any>) = l + t.size

    override fun doSomething(times: Int, what: Something) = what

    override fun validateReason(reason: String): String = if(reason == "Because") "Correct" else throw TestException("Bad reason: \"$reason\"")
}

class TestException(message: String): RuntimeException(message) {
    override fun equals(other: Any?): Boolean {
        return other != null && other is TestException && message == other.message
    }

    override fun hashCode(): Int {
        return message!!.hashCode()
    }
}

data class Something(val name: String, val color: String, val weight: BigDecimal, val tags: List<String>)

