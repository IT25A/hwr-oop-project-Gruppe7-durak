package hwr.oop.examples.template.core

@JvmInline
value class PlayerId(private val value: String) {

 }

class PlayerHand(
	val id: PlayerId,
	//private machen, weil sonst schlecht
	val cards: MutableList<Card> = mutableListOf()
) {

}