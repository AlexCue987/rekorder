package org.kollektions.proksy.model

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
