package hwr.oop.examples.template.core

@JvmInline
value class PlayerId(private val value: String) {

 }

class PlayerHand(
	val id: PlayerId,
	val cards: MutableList<Card> = mutableListOf()
) {

}

