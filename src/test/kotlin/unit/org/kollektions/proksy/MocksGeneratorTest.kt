package org.kollektions.proksy

import kotlin.test.Test
import kotlin.test.assertEquals

class MocksGeneratorTest {
    private val sut = MocksGenerator(OutputToMock("myMock", "MyMock"))

    @Test
    fun `firstResultStr generates result`() {
        val cases = listOf<Pair<IResult, String>>(
            Pair(ObjectResult(MyThing("Red", "Point")), "returns(MyThing(color = \"Red\",shape = \"Point\"))"),
            Pair(UnitResult(), "just(Runs)"),
            Pair(ExceptionResult(Exception("Oops!")), "throws(Exception({{val cause = null\n" +
                "val message = \"Oops!\"\n" +
                "Exception(cause,message)}}()))"))

        cases.forEach {
            assertEquals(it.second, sut.firstResultStr(it.first))
        }
    }

    @Test
    fun `nextResultStr generates result`() {
        val cases = listOf<Pair<IResult, String>>(
            Pair(ObjectResult(MyThing("Red", "Point")), "\n.andThen(MyThing(color = \"Red\",shape = \"Point\"))"),
            Pair(UnitResult(), "\n.andThen(Unit)"),
            Pair(ExceptionResult(Exception("Oops!")), "\n.andThenThrows(Exception({{val cause = null\n" +
                "val message = \"Oops!\"\n" +
                "Exception(cause,message)}}()))"))

        cases.forEach {
            assertEquals(it.second, sut.nextResultStr(it.first))
        }
    }

    @Test
    fun `stubStr without parameters`() {
        val actual = sut.stubStr("myMock",
            FunctionCallsSummary(FunctionCall("myFun", listOf(), UnitResult()) ))
        assertEquals("every{ myMock.myFun() }.\n", actual)
    }

    @Test
    fun `stubStr with parameters`() {
        val actual = sut.stubStr("myMock",
            FunctionCallsSummary(FunctionCall("myFun", listOf(42, "Oranges"), UnitResult()) ))
        assertEquals("every{ myMock.myFun(42,\n\"Oranges\") }.\n", actual)
    }

    @Test
    fun `resultsForOneListOfArguments when one result`() {
        val actual = sut.resultsForOneListOfArguments("myMock",
            FunctionCallsSummary(FunctionCall("myFun", listOf(42, "Oranges"), UnitResult()) ))
        assertEquals("every{ myMock.myFun(42,\n\"Oranges\") }.\njust(Runs)", actual)
    }

    @Test
    fun `resultsForOneListOfArguments when all results same, just Runs`() {
        val actual = sut.resultsForOneListOfArguments("myMock",
            FunctionCallsSummary("myFun", listOf(42, "Oranges"), mutableListOf(UnitResult(), UnitResult())))
        assertEquals("every{ myMock.myFun(42,\n\"Oranges\") }.\njust(Runs)", actual)
    }

    @Test
    fun `resultsForOneListOfArguments when all results same, returns 1`() {
        val actual = sut.resultsForOneListOfArguments("myMock",
            FunctionCallsSummary("myFun", listOf(42, "Oranges"),
                mutableListOf(ObjectResult(1), ObjectResult(1))))
        assertEquals("every{ myMock.myFun(42,\n\"Oranges\") }.\nreturns(1)", actual)
    }

    @Test
    fun `resultsForOneListOfArguments when all results same, throws TestException`() {
        val actual = sut.resultsForOneListOfArguments("myMock",
            FunctionCallsSummary("myFun", listOf(42, "Oranges"),
                mutableListOf(ExceptionResult(TestException("Ouch!")), ExceptionResult(TestException("Ouch!")))))
        assertEquals("every{ myMock.myFun(42,\n" +
            "\"Oranges\") }.\n" +
            "throws(TestException({{val cause = null\n" +
            "val message = \"Ouch!\"\n" +
            "TestException(cause,message)}}()))", actual)
    }

    @Test
    fun `resultsForOneListOfArguments when results different`() {
        val actual = sut.resultsForOneListOfArguments("myMock",
            FunctionCallsSummary("myFun", listOf(42, "Oranges"),
                mutableListOf(ObjectResult(1), ExceptionResult(TestException("Ouch!")), ObjectResult(2))))
        assertEquals("every{ myMock.myFun(42,\n" +
            "\"Oranges\") }.\n" +
            "returns(1)\n" +
            ".andThenThrows(TestException({{val cause = null\n" +
            "val message = \"Ouch!\"\n" +
            "TestException(cause,message)}}()))\n" +
            ".andThen(2)", actual)
    }
}
