package org.kollektions.proksy

import java.math.BigDecimal
import java.time.LocalDate

interface IRover {
    fun explore(a: Int): Int
    fun add(x: BigDecimal, d: LocalDate, s: String, l: Int): Int
    fun expand(l: Int, t: List<Any>): Int
    fun doSomething(times: Int, what: Something): Something
    fun doNothing(times: Int)
    fun validateReason(reason: String): String
    fun incrementCount(): Int
}
