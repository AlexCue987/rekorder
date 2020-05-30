package org.kollektions.proksy.generator.model

import java.time.LocalDate

data class MyThingWithMap(val name: String, val things: Map<LocalDate, MyThing>)
