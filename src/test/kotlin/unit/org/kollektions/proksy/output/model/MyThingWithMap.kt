package org.kollektions.proksy.output.model

import java.time.LocalDate

data class MyThingWithMap(val name: String, val things: Map<LocalDate, MyThing>)
