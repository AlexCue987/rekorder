package org.kollektions.proksy

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class OutputToMockTest {
    val sut = OutputToMock("myMock", "MyMock")

    @Test
    fun `outputInstance handles null`() {
        assertEquals("null", sut.output(null))
    }

    @Test
    fun `outputInstance handles field`() {
        assertEquals("LocalDate.of(2020, 5, 1)", sut.output(LocalDate.of(2020, 5, 1)))
    }

    @Test
    fun `outputInstance handles data class`() {
        val expected = "MyThing(color = \"Red\",shape = \"Square\")"
        assertEquals(expected, sut.output(MyThing("Red", "Square")))
    }

    @Test
    fun `outputInstance handles nested data class`() {
        val expected = "MyNestedThing(quantity = 1,thing = MyThing(color = \"Red\",shape = \"Square\"))"
        val actual = sut.output(MyNestedThing(1, MyThing("Red", "Square")))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles data class nested twice`() {
        val expected = "MyDoubleNestedThing(comment = \"double nested\"," +
            "nestedThing = MyNestedThing(quantity = 1," +
            "thing = MyThing(color = \"Red\"," +
            "shape = \"Square\")))"
        val actual = sut.output(MyDoubleNestedThing(comment = "double nested", nestedThing = MyNestedThing(1, MyThing("Red", "Square"))))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested list`() {
        val expected = "MyThingWithList(name = \"list of things\"," +
            "things = listOf(\nMyThing(color = \"Red\"," +
            "shape = \"Square\"),\nMyThing(color = \"Blue\"," +
            "shape = \"Circle\")\n))"
        val actual = sut.output(MyThingWithList("list of things",
               listOf(MyThing("Red", "Square"), MyThing("Blue", "Circle"))))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested set`() {
        val expected = "MyThingWithSet(name = \"list of things\",things = setOf(\n" +
            "MyThing(color = \"Red\",shape = \"Square\"),\n" +
            "MyThing(color = \"Blue\",shape = \"Circle\")\n" +
            "))"
        val actual = sut.output(MyThingWithSet("list of things",
            setOf(MyThing("Red", "Square"), MyThing("Blue", "Circle"))))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested map`() {
        val expected = "MyThingWithMap(name = \"list of things\"," +
            "things = mapOf(\n" +
            "LocalDate.of(2020, 5, 1) to MyThing(color = \"Red\",shape = \"Square\"),\n" +
            "LocalDate.of(2020, 5, 2) to MyThing(color = \"Blue\",shape = \"Circle\")\n))"
        val actual = sut.output(MyThingWithMap("list of things",
            mapOf(LocalDate.of(2020, 5, 1) to MyThing("Red", "Square"),
                LocalDate.of(2020, 5, 2) to MyThing("Blue", "Circle"))))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested list of maps`() {
        val expected = "MyThingWithListOfMaps(name = \"list of things\",things = listOf(\n" +
            "mapOf(\n" +
            "LocalTime.of(6, 10, 0) to MyThing(color = \"Red\",shape = \"Square\"),\n" +
            "LocalTime.of(7, 15, 0) to MyThing(color = \"Blue\",shape = \"Circle\")\n" +
            "),\n" +
            "mapOf(\n" +
            "LocalTime.of(7, 10, 0) to MyThing(color = \"Green\",shape = \"Square\"),\n" +
            "LocalTime.of(8, 15, 0) to MyThing(color = \"Amber\",shape = \"Circle\")\n" +
            ")\n" +
            "))"
        val actual = sut.output(MyThingWithListOfMaps("list of things",
            listOf(
                mapOf(LocalTime.of(6, 10) to MyThing("Red", "Square"),
                    LocalTime.of(7, 15) to MyThing("Blue", "Circle")),
                mapOf(LocalTime.of(7, 10) to MyThing("Green", "Square"),
                    LocalTime.of(8, 15) to MyThing("Amber", "Circle"))
            )))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `output handles private field`() {
        val withPrivateField = ClassWithPrivateField("Yellow", "Triangle")
        val actual = sut.output(withPrivateField)
        print(actual)
        val expected = "ClassWithPrivateField({{val color = \"Yellow\"\n" +
            "val shape = \"Triangle\"\n" +
            "ClassWithPrivateField(color,shape)}}())"
        assertEquals(expected, actual)
    }

    @Test
    fun `output uses only primary constructor parameters for data classes`() {
        val withExtraProperty = DataClassWithExtraProperty("Red", "Oval")
        val actual = sut.output(withExtraProperty)
        print(actual)
        val expected = "DataClassWithExtraProperty(color = \"Red\"," +
            "shape = \"Oval\")"
        assertEquals(expected, actual)
    }

    @Test
    fun `getFields returns all fields of data class without other properties`() {
        val withExtraProperty = MyThing("Red", "Oval")
        val actual = sut.getFields(withExtraProperty)
        assertEquals(listOf("color", "shape"), actual.map { it.name })
    }

    @Test
    fun `getFields returns all fields of data class with another property`() {
        val withExtraProperty = DataClassWithExtraProperty("Red", "Oval")
        val actual = sut.getFields(withExtraProperty)
        assertEquals(listOf("color", "shape"), actual.map { it.name })
    }

    @Test
    fun `getFields returns all properties of non-data class`() {
        val withExtraProperty = ClassWithPrivateFieldAndProperty("Red", "Oval")
        val actual = sut.getFields(withExtraProperty)
        assertEquals(setOf("color", "description", "shape"), actual.map { it.name }.toSet())
    }

    @Test
    fun `just runs`() {
        val sut = mockk<ThingWithUnitMethod>()

        every({sut.run()}).just(Runs)
            .andThenThrows(Exception("Oops"))
            .andThen(Unit)

//        every {sut.run()}.throws(Exception(""))
        (0..5).asSequence().forEach {
            try {
                val a  = sut.run()
                println("$it, $a")
            } catch (ex: Exception) {
                println("Caught ${ex.message}")
            }
        }
    }
}

data class MyThing(val color: String, val shape: String)

data class MyNestedThing(val quantity: Int, val thing: MyThing)

data class MyDoubleNestedThing(val comment: String, val nestedThing: MyNestedThing)

data class MyThingWithList(val name: String, val things: List<MyThing>)

data class MyThingWithSet(val name: String, val things: Set<MyThing>)

data class MyThingWithMap(val name: String, val things: Map<LocalDate, MyThing>)

data class MyThingWithListOfMaps(val name: String, val things: List<Map<LocalTime, MyThing>>)

class ClassWithPrivateField(val color: String, private val shape: String)

class ClassWithPrivateFieldAndProperty(val color: String, private val shape: String) {
    val description: String by lazy { "color=$color shape=$shape" }
}

data class DataClassWithExtraProperty(val color: String, val shape: String) {
    val description: String by lazy { "color=$color shape=$shape" }
}

class ThingWithUnitMethod() {
    fun run() {}
}
