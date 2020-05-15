package org.kollektions.proksy

import java.lang.Exception
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class CallRecorder {
    private val functionCalls = mutableListOf<FunctionCall>()

    fun save(functionCall: FunctionCall) = functionCalls.add(functionCall)

    fun getCalls() = functionCalls.toList()

    companion object {
        @JvmStatic
        inline fun<reified T> getProxy(instance: Any, callRecorder: CallRecorder): T {
            val proxy = Proxy.newProxyInstance(
                T::class.java.classLoader,
                arrayOf(T::class.java)) { proxy, method, args ->
                try {
                    val ret: Any? = method!!.invoke(instance, *(args ?: arrayOfNulls<Any>(0)))
                    val result = if (ret == null) UnitResult() else ObjectResult(ret!!)
                    callRecorder.save(FunctionCall(method.name, args.toList(), result))
                    ret
                } catch (ex: InvocationTargetException) {
                    val targetException = ex.targetException
                    val cause = targetException.cause
                    callRecorder.save(FunctionCall(method.name, args.toList(), ExceptionResult(targetException!!)))
                    throw targetException
                }
            } as T
            return proxy
        }
    }
}

class OutputToMock(val instanceName: String, val className: String) {
    val calls = mutableListOf<String>()
    val functionCalls = mutableListOf<FunctionCall>()
    val classes = mutableSetOf<String>("io.mockk.every", "io.mockk.mockk")
    val outputters = listOf<(arg: Any) -> Optional<String>>()
    val ignoredFieldsMap = mutableMapOf<String, List<String>>()

    fun customOutput(arg: Any?) : Optional<String> {
        if (arg != null) {
            for(outputter in outputters) {
                val ret = outputter(arg)
                if (ret.isPresent) {
                    return ret
                }
            }
            return Optional.empty()
        }
        return Optional.empty()
    }

//    fun addOutputter(className: String, outputter: (arg: Any) -> String) { outputters[className] = outputter }

//    fun ignoreFieldsForClass(className: String, ignoredFields: List<String>) {
//        addOutputter(className) { arg: Any -> outputInstance(arg, ignoredFields)}
//    }

//    fun inCustomOutput(arg: Any): Boolean {
//        for(clazz in outputters.keys) {
//            print(clazz)
//            if(arg is clazz.) {
//                return true
//            }
//        }
//        return false
//    }

    fun save(method: Method, args: List<Any>, result: Any) {
        val argsList = argsAsCsv(args)
        val line = "every { $instanceName.${method.name}($argsList) } returns ${output(result)}"
        calls.add(line)
    }

    fun saveWithMethodName(args: List<Any>, methodName: String, result: Any) {
        val argsList = argsAsCsv(args)
        val line = "every { $instanceName.$methodName($argsList) } returns ${output(result)}"
        calls.add(line)
    }

    fun toMethod(): String {
        val distinctCalls = mutableListOf<String>()
        calls.forEach { if(it !in distinctCalls) distinctCalls.add(it)}
        return "${getImports(classes)}\n\nfun get$className(): $className {\n    val $instanceName = mockk<$className>()\n    ${distinctCalls.joinToString("\n    ")}\n    return $instanceName\n}"
    }

    fun getImports(classes: Set<String>): String {
        val imports = classes.map { "import $it" }.sorted()
        return imports.joinToString(  "\n" )
    }

    fun argsAsCsv(args: List<*>): String {
        val argsList = args.joinToString(",\n") { output(it) }
        return argsList
    }

    fun argsAsMap(args: Map<*, *>): String {
        val argsList = args.entries.map {
            val value = "${output(it.key)} to ${output(it.value)}"
            value
        } .joinToString(",\n")
        return argsList
    }

    fun output(arg: Any?): String {
        if(arg == null) return "null"
        classes.add(arg.javaClass.name)
        val customOutputStr = customOutput(arg)
        if(customOutputStr.isPresent) {
            return customOutputStr.get()
        }
        return when {
            arg.javaClass.isEnum -> "${arg.javaClass.simpleName}.$arg"
            arg is Int -> arg.toString()
            arg is Boolean -> arg.toString()
            arg is Long -> "${arg}L"
            arg is String -> "\"$arg\""
            arg is LocalDate -> "LocalDate.of(${arg.year}, ${arg.monthValue}, ${arg.dayOfMonth})"
            arg is LocalTime -> "LocalTime.of(${arg.hour}, ${arg.minute}, ${arg.second})"
            arg is LocalDateTime -> "LocalDateTime.of(${arg.year}, ${arg.monthValue}, ${arg.dayOfMonth}, ${arg.hour}, ${arg.minute}, ${arg.second})"
            arg is BigDecimal -> "BigDecimal(\"${arg.toPlainString()}\")"
            arg is List<*> -> "listOf(\n${argsAsCsv(arg)}\n)"
            arg is Set<*> -> "setOf(\n${argsAsCsv(arg.toList())}\n)"
            arg is Map<*, *> -> "mapOf(\n${argsAsMap(arg)}\n)"
            else -> outputInstance(arg)
        }
    }

    fun outputInstance(arg: Any): String {
        val className = arg.javaClass.simpleName
        val fields = getFields(arg)
        val nameValuePairs = fieldsWithValues(arg, fields)
        return if(arg::class.isData) {
            val namedParams = nameValuePairs.map { "${it.first} = ${output(it.second)}" }
            "$className(${namedParams.joinToString(",")})"
        }
        else {
            val variableDefinitions = nameValuePairs.map { "val ${it.first} = ${output(it.second)}" }
            val variableNames = nameValuePairs.map { it.first }
            "$className({{${variableDefinitions.joinToString("\n")}\n" +
                "$className(${variableNames.joinToString(",")})}}())"
        }
    }

    fun getFields(arg: Any): Collection<KProperty1<out Any, *>> {
        val fields = arg::class.memberProperties
        if(arg::class.isData) {
            val klass = arg.javaClass.kotlin
            val primaryConstructor = klass.primaryConstructor
            val params = primaryConstructor!!.parameters
            return params.map { param -> fields.first { field -> field.name == param.name} }
        }
        return fields
    }

    fun fieldsWithValues(intstance: Any, fields: Collection<KProperty1<out Any, *>>): List<Pair<String, Any?>> {
        return fields.asSequence()
            .map {
                it.getter.isAccessible = true
                val value = it.getter.call(intstance)
                Pair(it.name, value)
            }
            .toList()
    }
}

data class FunctionCall(val functionName: String, val arguments: List<Any>, val result: IResult)

data class FunctionCallsSummary(val functionName: String,
                                val arguments: List<Any>,
                                val results: MutableList<IResult>) {
    constructor(functionCall: FunctionCall): this(functionCall.functionName,
        functionCall.arguments, mutableListOf(functionCall.result))

    init {
        require(results.isNotEmpty()) { "Results cannot be empty" }
    }

    fun addResult(result: IResult) {
        results.add(result)
    }
}

class CallsOrganizer{
    fun organize(functionCalls: List<FunctionCall>): List<FunctionCallsSummary>{
        val savedCalls = mutableListOf<FunctionCallsSummary>()
        for(functionCall in functionCalls) {
            addCall(savedCalls, functionCall)
        }
        return savedCalls
    }

    private fun addCall(savedCalls: MutableList<FunctionCallsSummary>, functionCall: FunctionCall) {
        for (savedCall in savedCalls) {
            if (savedCall.functionName == functionCall.functionName
                        && savedCall.arguments == functionCall.arguments) {
                savedCall.addResult(functionCall.result)
                return
            }
        }
        savedCalls.add(FunctionCallsSummary(functionCall))
    }
}

class MocksGenerator(val outputToMock: OutputToMock) {
    fun generateCallsOfEvery(mockName: String, calls: List<FunctionCallsSummary>): String {
        throw Exception("")
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
        val stub = "every({ $mockName.${call.functionName}($args) }).\n"
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
