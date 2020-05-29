package org.kollektions.proksy.output

import org.kollektions.proksy.model.*

class MocksGenerator(private val outputToMock: OutputToMock2) {
    fun generateCallsOfEvery(mockName: String, calls: List<FunctionCallsSummary>, className: String): GeneratedCode {
        val mocks = calls.map { resultsForOneListOfArguments(mockName, it) }.toList()
        val commandsOfEvery = mocks.map { it.code }.joinToString("\n\n")
        val allImports = mergeSets(mocks.map { it.classesToImport }.asSequence())
        return GeneratedCode(allImports, "fun get${uppercaseFirstChar(mockName)}(): $className{\n" +
            "val $mockName = mockk<$className>()\n" +
            "$commandsOfEvery\n" +
            "return $mockName\n}")
    }

    fun uppercaseFirstChar(name: String): String {
        val firstChar = name.substring(0..0).toUpperCase()
        val tail = name.substring(1)
        return "$firstChar$tail"
    }

    fun resultsForOneListOfArguments(mockName: String, call: FunctionCallsSummary): GeneratedCode {
        val stub = oneResultStr(mockName, call)
        if (call.results.distinct().size == 1) {
            return stub
        }
        var ret = stub
        (1 until call.results.size).asSequence().forEach {
            val result = call.results[it]
            ret = mergeGeneratedCode(ret, nextResultStr(result))
        }
        return ret
    }

    fun oneResultStr(mockName: String, call: FunctionCallsSummary): GeneratedCode {
        val stub = stubStr(mockName, call)
        val firstResult = call.results.first()
        val firstResultStr = firstResultStr(firstResult)
        val imports = mergeSets(stub.classesToImport, firstResultStr.classesToImport)
        return GeneratedCode(imports, stub.code + firstResultStr.code)
    }

    fun stubStr(mockName: String, call: FunctionCallsSummary): GeneratedCode {
        val args = call.arguments.asSequence()
            .map { outputToMock.output(it) }
            .toList()
        val argsClasses = args.map { it.classesToImport }.flatten().toSet()
        val argsStr = args.map {it.code}. joinToString(",\n")
        val stub = "every{ $mockName.${call.functionName}($argsStr) }.\n"
        return GeneratedCode(argsClasses, stub)
    }

    fun firstResultStr(firstResult: IResult): GeneratedCode {
        return when (firstResult) {
            is ObjectResult -> {
                val output = outputToMock.output(firstResult.result)
                GeneratedCode(output.classesToImport,"returns(${output.code})")
            }
            is UnitResult -> GeneratedCode(setOf(), "just(Runs)")
            is ExceptionResult -> {
                val output = outputToMock.output(firstResult.exception)
                GeneratedCode(output.classesToImport, "throws(${output.code})")
            }
        }
    }

    fun nextResultStr(result: IResult): GeneratedCode {
        return when (result) {
            is ObjectResult -> {
                val output = outputToMock.output(result.result)
                formatGeneratedCode(output) {"\n.andThen($it)"}
            }
            is ExceptionResult -> {
                val output = outputToMock.output(result.exception)
                formatGeneratedCode(output) {"\n.andThenThrows($it)"}
            }
            is UnitResult -> GeneratedCode(setOf(), "\n.andThen(Unit)")
        }
    }
}

fun<T> mergeSets(set1: Iterable<T>, set2: Iterable<T>): Set<T> {
    val ret = set1.toMutableSet()
    ret.addAll(set2)
    return ret.toSet()
}

fun<T> mergeSets(sets: Sequence<Set<T>>): Set<T> {
   return sets.asSequence().flatten().toSet()
}

fun formatGeneratedCode(code: GeneratedCode, formatter: (input: String) -> String) =
    GeneratedCode(code.classesToImport, formatter(code.code))

fun mergeGeneratedCode(code1: GeneratedCode, code2: GeneratedCode) =
    GeneratedCode(mergeSets(code1.classesToImport, code2.classesToImport), code1.code + code2.code)
