package org.kollektions.proksy.generator

import org.kollektions.proksy.model.FunctionCall
import org.kollektions.proksy.model.FunctionCallsSummary

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
