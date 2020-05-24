package org.kollektions.proksy.output

import org.kollektions.proksy.CallRecorder
import org.kollektions.proksy.CallsOrganizer
import org.kollektions.proksy.mocksGenerator
import java.io.File

class PrintMockks(val className: String, val recorder: CallRecorder) {
    val fileName: String = "$className.kt"

    init {
        val file = File(fileName)
        if(!file.exists()) {
            file.writeText("import io.mockk.every\n" +
                "import io.mockk.mockk\n" +
                "\n" +
                "class $className {\n" +
                "}")
        }
    }

    fun appendMethod(methodBody: String) {
        val file = File(fileName)
        val existingText = file.readText()
        val textToWrite = "${existingText.substring(0..existingText.length - 2)}\n$methodBody\n}"
        file.writeText(textToWrite)
    }

    fun flushAndPrint(mockName: String, className: String) {
        val calls = recorder.flush()
        val callSummaries = CallsOrganizer().organize(calls)
        val generator = mocksGenerator("rover", className)
        val mocks = generator.generateCallsOfEvery(mockName, callSummaries, className)
        appendMethod(mocks)
    }
}
