package org.kollektions.proksy.output.model

data class DataClassWithExtraProperty(val color: String, val shape: String) {
    val description: String by lazy { "color=$color shape=$shape" }
}
