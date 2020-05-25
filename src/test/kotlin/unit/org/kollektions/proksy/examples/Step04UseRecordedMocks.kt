package org.kollektions.proksy.examples

import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class Step04UseRecordedMocks {

    private val mocks = MyMockks()

    @Test
    fun test1() {
        val mockRover = mocks.getThisRover1()
        val actual = mockRover.explore(1)
        assertEquals(2, actual)
    }

    @Test
    fun test2() {
        val mockRover = mocks.getThisRover2()
        val actual = mockRover.add(BigDecimal.ONE, LocalDate.MIN, "Any", 1)
        assertEquals(2, actual)
    }
}
