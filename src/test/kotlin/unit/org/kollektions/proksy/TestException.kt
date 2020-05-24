package org.kollektions.proksy

class TestException(message: String): RuntimeException(message) {
    override fun equals(other: Any?): Boolean {
        return other != null && other is TestException && message == other.message
    }

    override fun hashCode(): Int {
        return message!!.hashCode()
    }
}
