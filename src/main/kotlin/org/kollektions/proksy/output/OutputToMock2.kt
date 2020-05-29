package org.kollektions.proksy.output

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class OutputToMock2() {
    val classes = mutableSetOf<String>("io.mockk.every", "io.mockk.mockk")
    val outputters = listOf<(arg: Any) -> Optional<String>>()

    fun customOutput(arg: Any?) : Optional<GeneratedCode> {
        if (arg != null) {
            for(outputter in outputters) {
                val ret = outputter(arg)
                if (ret.isPresent) {
                    return Optional.empty()
                }
            }
            return Optional.empty()
        }
        return Optional.empty()
    }

    fun argsAsCsv(args: List<*>): GeneratedCode {
        val items = args.map { output(it) }
        val classNames = items.asSequence().map { it.classesToImport }.flatten().toSet()
        val argsList = items.map{ it.code }.joinToString(",\n")
        return GeneratedCode(classNames, argsList)
    }

    fun argsAsMap(args: Map<*, *>): GeneratedCode {
        val entries = args.entries.map { outputMapEntry(it) }
        val argsList = entries.map{ it.code }.joinToString(",\n")
        val classes = entries.asSequence().map { it.classesToImport }.flatten().toSet()
        return GeneratedCode(classes, argsList)
    }

    fun outputMapEntry(entry: Map.Entry<*, *>): GeneratedCode {
        val keyCode = output(entry.key)
        val valueCode = output(entry.value)
        val pair = "${keyCode.code} to ${valueCode.code}"
        val classes = keyCode.classesToImport.toMutableSet()
        classes.addAll(valueCode.classesToImport)
        return GeneratedCode(classes, pair)
    }

    fun output(arg: Any?): GeneratedCode {
        if(arg == null) return GeneratedCode(setOf(), "null")
        classes.add(arg.javaClass.name)
        val customOutputStr = customOutput(arg)
        if(customOutputStr.isPresent) {
            return customOutputStr.get()
        }
        return when {
            arg.javaClass.isEnum -> GeneratedCode(setOf(),"${arg.javaClass.simpleName}.$arg")
            arg is Int -> GeneratedCode(setOf(),arg.toString())
            arg is Boolean -> GeneratedCode(setOf(),arg.toString())
            arg is Long -> GeneratedCode(setOf(),"${arg}L")
            arg is String -> GeneratedCode(setOf(),"\"$arg\"")
            arg is LocalDate -> GeneratedCode(setOf("java.time.LocalDate"),"LocalDate.of(${arg.year}, ${arg.monthValue}, ${arg.dayOfMonth})")
            arg is LocalTime -> GeneratedCode(setOf("java.time.LocalTime"),"LocalTime.of(${arg.hour}, ${arg.minute}, ${arg.second})")
            arg is LocalDateTime -> GeneratedCode(setOf("java.time.LocalDateTime"),"LocalDateTime.of(${arg.year}, ${arg.monthValue}, ${arg.dayOfMonth}, ${arg.hour}, ${arg.minute}, ${arg.second})")
            arg is BigDecimal -> GeneratedCode(setOf("java.math.BigDecimal"),"BigDecimal(\"${arg.toPlainString()}\")")
            arg is List<*> -> generatedCodeOfList(argsAsCsv(arg))
            arg is Set<*> -> generatedCodeOfSet(argsAsCsv(arg.toList()))
            arg is Map<*, *> -> generatedCodeOfMap(argsAsMap(arg))
            else -> outputInstance(arg)
        }
    }

    fun outputInstance(arg: Any): GeneratedCode {
        val className = arg.javaClass.simpleName
        val fields = getFields(arg)
        val nameValuePairs = fieldsWithValues(arg, fields)
        val nameOutputPairs = nameValuePairs.asSequence()
            .map { it.first to output(it.second) }
            .toList()
        val classesToImport = nameOutputPairs.asSequence()
            .map { it.second.classesToImport }
            .flatten()
            .toMutableSet()
        classesToImport.add(arg.javaClass.name)
        val code = if(arg::class.isData) {
            val namedParams = nameOutputPairs.map { "${it.first} = ${it.second.code}" }
            "$className(${namedParams.joinToString(",")})"
        }
        else {
            val variableDefinitions = nameOutputPairs.map { "val ${it.first} = ${it.second.code}" }
            val variableNames = nameOutputPairs.map { it.first }
            "$className({{${variableDefinitions.joinToString("\n")}\n" +
                "$className(${variableNames.joinToString(",")})}}())"
        }
        return GeneratedCode(classesToImport, code)
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

//fun mocksGenerator2(instanceName: String, className: String) = MocksGenerator(OutputToMock2())

