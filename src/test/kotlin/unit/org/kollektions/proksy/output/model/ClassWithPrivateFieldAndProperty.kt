package org.kollektions.proksy.output.model

class ClassWithPrivateFieldAndProperty(val color: String, private val shape: String) {
    val description: String by lazy { "color=$color shape=$shape" }
}
