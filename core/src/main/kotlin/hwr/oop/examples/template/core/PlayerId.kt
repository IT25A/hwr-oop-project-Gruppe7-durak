package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PlayerId(private val value: String) {

}