package org.kollektions.proksy.testmodel

class TestException(cause: Throwable?, message: String): RuntimeException(message, cause) {
    constructor(message: String): this(null, message)

    override fun equals(other: Any?): Boolean {
        return other != null && other is TestException && message == other.message
    }

    override fun hashCode(): Int {
        return message!!.hashCode()
    }
}
