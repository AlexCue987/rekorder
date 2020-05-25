package org.kollektions.proksy.output

import org.kollektions.proksy.model.*

class MocksGenerator(val outputToMock: OutputToMock) {
    fun generateCallsOfEvery(mockName: String, calls: List<FunctionCallsSummary>, className: String): String {
        val commandsOfEvery = calls.map { resultsForOneListOfArguments(mockName, it) }.joinToString("\n\n")
        return "fun get${uppercaseFirstChar(mockName)}(): $className{\n" +
            "val $mockName = mockk<$className>()\n" +
            "$commandsOfEvery\n" +
            "return $mockName\n}"
    }

    fun uppercaseFirstChar(name: String): String {
        val firstChar = name.substring(0..0).toUpperCase()
        val tail = name.substring(1)
        return "$firstChar$tail"
    }

    fun resultsForOneListOfArguments(mockName: String, call: FunctionCallsSummary): String {
        val stub = oneResultStr(mockName, call)
        if (call.results.distinct().size == 1) {
            return stub
        }
        var ret = stub
        (1 until call.results.size).asSequence().forEach {
            val result = call.results[it]
            ret += nextResultStr(result)
        }
        return ret
    }

    fun oneResultStr(mockName: String, call: FunctionCallsSummary): String {
        val stub = stubStr(mockName, call)
        val firstResult = call.results.first()
        return stub + firstResultStr(firstResult)
    }

    fun stubStr(mockName: String, call: FunctionCallsSummary): String {
        val args = call.arguments.asSequence()
            .map { outputToMock.output(it) }
            .joinToString(",\n")
        val stub = "every{ $mockName.${call.functionName}($args) }.\n"
        return stub
    }

    fun firstResultStr(firstResult: IResult): String {
        return when (firstResult) {
            is ObjectResult -> "returns(${outputToMock.output(firstResult.result)})"
            is UnitResult -> "just(Runs)"
            is ExceptionResult -> "throws(${outputToMock.output(firstResult.exception)})"
        }
    }

    fun nextResultStr(result: IResult): String {
        return when (result) {
            is ObjectResult -> "\n.andThen(${outputToMock.output(result.result)})"
            is ExceptionResult -> "\n.andThenThrows(${outputToMock.output(result.exception)})"
            is UnitResult -> "\n.andThen(Unit)"
        }
    }
}
