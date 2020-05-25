package org.kollektions.proksy.examples

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class Step02UseOneMock {
    val mockRover = MyMockks2().getThisRover()

    @Test
    fun test1() {
        val actual = mockRover.explore(1)
        assertEquals(2, actual)
    }

    @Test
    fun test2() {
        val actual = mockRover.add(BigDecimal.ONE, LocalDate.MIN, "Any", 1)
        assertEquals(2, actual)
    }
}
