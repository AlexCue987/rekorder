package org.kollektions.proksy

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.full.memberProperties

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
                    val wrappedException = ExceptionResult(targetException!!)
                    callRecorder.save(FunctionCall(method.name, args.toList(), wrappedException))
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
        val argsList = args.joinToString(", ") { output(it) }
        return argsList
    }

    fun argsAsMap(args: Map<*, *>): String {
        val argsList = args.entries.map {
            val value = "${output(it.key)} to ${output(it.value)}"
            value
        } .joinToString(", \n")
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
            arg is LocalDateTime -> "LocalDateTime.of(${arg.year}, ${arg.monthValue}, ${arg.dayOfMonth}, ${arg.hour}, ${arg.minute}, ${arg.second})"
            arg is BigDecimal -> "BigDecimal(\"${arg.toPlainString()}\")"
            arg is List<*> -> "listOf(${argsAsCsv(arg)})"
            arg is Set<*> -> "setOf(${argsAsCsv(arg.toList())})"
            arg is Map<*, *> -> "mapOf(${argsAsMap(arg)})"
            else -> outputInstance(arg)
        }
    }

    fun outputInstance(arg: Any): String {
        val className = arg.javaClass.simpleName
        val ignoredFields = if(className in ignoredFieldsMap) ignoredFieldsMap[className] else listOf()
        val fields = arg::class.memberProperties
        val declaredFields = arg.javaClass.declaredFields
        val fieldValues = fields
            .filter { it.name !in ignoredFields!! }
            .map {
                print("Accessing $className.${it.name}\n")
                val value = it.getter.call(arg)
                "${it.name} = ${output(value)}"
            }
        return "$className(${fieldValues.joinToString( ", \n")})"
    }

}

interface IResult{}

class UnitResult: IResult {
    override fun equals(other: Any?) = other is UnitResult
}

data class ExceptionResult(val exception: Throwable): IResult  {
    override fun equals(other: Any?) = other is ExceptionResult
        && exception.javaClass.name == other.exception.javaClass.name
        && exception.message == other.exception.message
}

data class ObjectResult(val result: Any): IResult

data class FunctionCall(val functionName: String, val arguments: List<Any>, val result: IResult) {
}
