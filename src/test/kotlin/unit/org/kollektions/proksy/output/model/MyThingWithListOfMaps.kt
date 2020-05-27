package org.kollektions.proksy.output.model

import java.time.LocalTime

data class MyThingWithListOfMaps(val name: String, val things: List<Map<LocalTime, MyThing>>)
