package org.kollektions.proksy

import java.math.BigDecimal
import java.time.LocalDate

class Rover() : IRover {
    var count = 0

    override fun doNothing(times: Int) {}

    override fun explore(a: Int): Int {
        println("Exploring $a")
        return a+1
    }

    override fun incrementCount(): Int {
        return if (++count == 2) throw TestException("Not 2") else count
    }

    override fun add(x: BigDecimal, d: LocalDate, s: String, l: Int) = l+1

    override fun expand(l: Int, t: List<Any>) = l + t.size

    override fun doSomething(times: Int, what: Something) = what

    override fun validateReason(reason: String): String = if(reason == "Because") "Correct" else throw TestException("Bad reason: \"$reason\"")
}
