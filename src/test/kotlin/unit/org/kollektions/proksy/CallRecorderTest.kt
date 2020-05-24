package org.kollektions.proksy

import io.mockk.every
import io.mockk.mockk
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.kollektions.proksy.CallRecorder.Companion.getProxy
import java.math.BigDecimal
import kotlin.test.*

class CallRecorderTest {
    val sut = CallRecorder()
    val instanceToMock = Rover()
    val proxy = getProxy<IRover>(instanceToMock, sut)

    @Before
    fun before() {
        println("Before")
    }

    @AfterTest
    fun afterTest() {
        println("After test")
    }

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

    companion object {

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            println("Before class")
        }

        @JvmStatic
        @AfterClass
        fun afterClass() {
            println("After Class")
//            val calls = sut.getCalls()
//            calls.forEach { println(it) }
        }
    }

}

