package org.kollektions.proksy

sealed class IResult

class UnitResult: IResult() {
    override fun equals(other: Any?) = other is UnitResult

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

data class ExceptionResult(val exception: Throwable): IResult() {
    override fun equals(other: Any?) = other is ExceptionResult
        && exception.javaClass.name == other.exception.javaClass.name
        && exception.message == other.exception.message

    override fun hashCode(): Int {
        return exception.hashCode()
    }
}

data class ObjectResult(val result: Any): IResult()
