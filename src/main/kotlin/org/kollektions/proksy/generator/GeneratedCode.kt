package org.kollektions.proksy.generator

data class GeneratedCode(val classesToImport: Set<String>, val code: String)

fun generatedCodeOfList(listCode: GeneratedCode) =
    GeneratedCode(addItemToSet(listCode.classesToImport, "java.util.List"),
    "listOf(\n${listCode.code}\n)")

fun generatedCodeOfSet(setCode: GeneratedCode) =
    GeneratedCode(addItemToSet(setCode.classesToImport, "java.util.Set"),
        "setOf(\n${setCode.code}\n)")

fun generatedCodeOfMap(mapCode: GeneratedCode) =
    GeneratedCode(addItemToSet(mapCode.classesToImport, "java.util.Map"),
        "mapOf(\n${mapCode.code}\n)")

fun<T> addItemToSet(set: Set<T>, item: T): Set<T> {
    val ret = set.toMutableList()
    ret.add(item)
    return ret.toSet()
}
