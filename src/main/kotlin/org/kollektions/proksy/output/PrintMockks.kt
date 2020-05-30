package org.kollektions.proksy.output

import org.kollektions.proksy.recorder.CallRecorder
import java.io.File

class PrintMockks(val className: String,
                  val recorder: CallRecorder,
                  interfaceName: String,
                  path: String,
                  packageName: String) {
    val fileName: String = "$path/${packageName.replace('.', '/')}/$className.kt"

    init {
        val file = File(fileName)
        if(!file.exists()) {
            file.writeText("package $packageName\n" +
                "\n" +
                "import io.mockk.every\n" +
                "import io.mockk.mockk\n" +
                "import $interfaceName\n" +
                "\n" +
                "class $className {\n" +
                "}")
        }
    }

    fun appendMock(methodBody: GeneratedCode) {
        val file = File(fileName)
        val lines = file.readLines()
        val packageCommand = lines[0]
        val importsAndClass = lines.subList(2, lines.size)
        val importsInFile = importsAndClass.takeWhile { it.startsWith("import ") }
        val newImports = methodBody.classesToImport.map { "import $it" }
        val klassBody = importsAndClass.filter { !it.startsWith("import ") }
        val klassBodyStr = klassBody.subList(0, klassBody.size - 1).joinToString("\n")
        val importsToSave = mergeSets(importsInFile, newImports).asSequence().sorted().toList().joinToString("\n")
        val textToWrite = "$packageCommand\n\n$importsToSave\n\n$klassBodyStr\n${methodBody.code}\n}"
        file.writeText(textToWrite)
    }

    fun flushAndPrint(mockName: String, className: String) {
        val calls = recorder.flush()
        val callSummaries = CallsOrganizer().organize(calls)
        val generator = mocksGenerator("rover", className)
        val mocks = generator.generateCallsOfEvery(mockName, callSummaries, className)
        appendMock(mocks)
    }

    fun<T> mergeLists(list1: Sequence<T>, list2: Sequence<T>): List<T> {
        val ret = list1.toMutableList()
        ret.addAll(list2)
        return ret.toList()
    }
}
