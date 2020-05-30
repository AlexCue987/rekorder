package org.kollektions.proksy.generator.model

import java.time.LocalTime

data class MyThingWithListOfMaps(val name: String, val things: List<Map<LocalTime, MyThing>>)
