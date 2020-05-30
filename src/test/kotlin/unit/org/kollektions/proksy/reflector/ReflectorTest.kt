package org.kollektions.proksy.reflector

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.kollektions.proksy.generator.GeneratedCode
import org.kollektions.proksy.generator.model.*
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class ReflectorTest {
    val sut2 = Reflector()

    @Test
    fun `outputInstance handles null`() {
        assertEquals(GeneratedCode(setOf(), "null"), sut2.output(null))
    }

    @Test
    fun `outputInstance handles field`() {
        val expected = GeneratedCode(setOf("java.time.LocalDate"), "LocalDate.of(2020, 5, 1)")
        assertEquals(expected, sut2.output(LocalDate.of(2020, 5, 1)))
    }

    @Test
    fun `outputInstance handles data class`() {
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.MyThing"), "MyThing(color = \"Red\",shape = \"Square\")")
        assertEquals(expected, sut2.output(MyThing("Red", "Square")))
    }

    @Test
    fun `outputInstance handles nested data class`() {
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.MyThing",
            "org.kollektions.proksy.output.model.MyNestedThing"),
            "MyNestedThing(quantity = 1,thing = MyThing(color = \"Red\",shape = \"Square\"))")
        val actual = sut2.output(MyNestedThing(1, MyThing("Red", "Square")))
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles data class nested twice`() {
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.MyThing",
            "org.kollektions.proksy.output.model.MyNestedThing",
            "org.kollektions.proksy.output.model.MyDoubleNestedThing"),
            "MyDoubleNestedThing(comment = \"double nested\"," +
                "nestedThing = MyNestedThing(quantity = 1," +
                "thing = MyThing(color = \"Red\"," +
                "shape = \"Square\")))")
        val actual = sut2.output(MyDoubleNestedThing(comment = "double nested", nestedThing = MyNestedThing(1, MyThing("Red", "Square"))))
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested list`() {
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.MyThing",
            "java.util.List",
            "org.kollektions.proksy.output.model.MyThingWithList"),
            "MyThingWithList(name = \"list of things\"," +
                "things = listOf(\nMyThing(color = \"Red\"," +
                "shape = \"Square\"),\nMyThing(color = \"Blue\"," +
                "shape = \"Circle\")\n))")
        val actual = sut2.output(MyThingWithList("list of things",
            listOf(MyThing("Red", "Square"), MyThing("Blue", "Circle"))))
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested set`() {
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.MyThing",
            "java.util.Set",
            "org.kollektions.proksy.output.model.MyThingWithSet"),
            "MyThingWithSet(name = \"list of things\",things = setOf(\n" +
                "MyThing(color = \"Red\",shape = \"Square\"),\n" +
                "MyThing(color = \"Blue\",shape = \"Circle\")\n" +
                "))")
        val actual = sut2.output(MyThingWithSet("list of things",
            setOf(MyThing("Red", "Square"), MyThing("Blue", "Circle"))))
        print(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested map`() {
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.MyThing",
            "java.util.Map",
            "org.kollektions.proksy.output.model.MyThingWithMap",
            "java.time.LocalDate"),
            "MyThingWithMap(name = \"list of things\"," +
                "things = mapOf(\n" +
                "LocalDate.of(2020, 5, 1) to MyThing(color = \"Red\",shape = \"Square\"),\n" +
                "LocalDate.of(2020, 5, 2) to MyThing(color = \"Blue\",shape = \"Circle\")\n))")
        val actual = sut2.output(MyThingWithMap("list of things",
            mapOf(LocalDate.of(2020, 5, 1) to MyThing("Red", "Square"),
                LocalDate.of(2020, 5, 2) to MyThing("Blue", "Circle"))))
        assertEquals(expected, actual)
    }

    @Test
    fun `outputInstance handles nested list of maps`() {
        val expectedCode = "MyThingWithListOfMaps(name = \"list of things\",things = listOf(\n" +
            "mapOf(\n" +
            "LocalTime.of(6, 10, 0) to MyThing(color = \"Red\",shape = \"Square\"),\n" +
            "LocalTime.of(7, 15, 0) to MyThing(color = \"Blue\",shape = \"Circle\")\n" +
            "),\n" +
            "mapOf(\n" +
            "LocalTime.of(7, 10, 0) to MyThing(color = \"Green\",shape = \"Square\"),\n" +
            "LocalTime.of(8, 15, 0) to MyThing(color = \"Amber\",shape = \"Circle\")\n" +
            ")\n" +
            "))"
        val expected = GeneratedCode(setOf(
            "java.time.LocalTime",
            "org.kollektions.proksy.output.model.MyThing",
            "java.util.List",
            "java.util.Map",
            "org.kollektions.proksy.output.model.MyThingWithListOfMaps"), expectedCode)
        val actual = sut2.output(MyThingWithListOfMaps("list of things",
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
        val actual = sut2.output(withPrivateField)
        val expectedCode = "{val color = \"Yellow\"\n" +
            "val shape = \"Triangle\"\n" +
            "ClassWithPrivateField(color,shape)}()"
        val expected = GeneratedCode(setOf("org.kollektions.proksy.output.model.ClassWithPrivateField"), expectedCode)
        assertEquals(expected, actual)
    }

    @Test
    fun `output uses only primary constructor parameters for data classes`() {
        val withExtraProperty = DataClassWithExtraProperty("Red", "Oval")
        val actual = sut2.output(withExtraProperty)
        val expected = GeneratedCode(setOf(
            "org.kollektions.proksy.output.model.DataClassWithExtraProperty"),
            "DataClassWithExtraProperty(color = \"Red\"," +
                "shape = \"Oval\")")
        assertEquals(expected, actual)
    }

    @Test
    fun `getFields returns all fields of data class without other properties`() {
        val withExtraProperty = MyThing("Red", "Oval")
        val actual = sut2.getFields(withExtraProperty)
        assertEquals(listOf("color", "shape"), actual.map { it.name })
    }

    @Test
    fun `getFields returns all fields of data class with another property`() {
        val withExtraProperty = DataClassWithExtraProperty("Red", "Oval")
        val actual = sut2.getFields(withExtraProperty)
        assertEquals(listOf("color", "shape"), actual.map { it.name })
    }

    @Test
    fun `getFields returns all properties of non-data class`() {
        val withExtraProperty = ClassWithPrivateFieldAndProperty("Red", "Oval")
        val actual = sut2.getFields(withExtraProperty)
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

    @Ignore
    @Test
    fun writes() {
        val file = File("/Users/z002w5y/fun/rekorder/src/test/kotlin/unit/org/kollektions/proksy/examples/mocks/m.kt")
        if(!file.exists()) {
            file.writeText("import io.mockk.every\n" +
                "import io.mockk.mockk\n" +
                "import o\n" +
                "\n" +
                "class z {\n" +
                "}")
        }

    }
}

