package hwr.oop.examples.template.core

@JvmInline
value class PlayerId(private val value: String) {
	
 }
/*

 class PlayerCard(val card: MutableList<Card> = mutableListOf()) {
	fun add(card: Card) {
		card.add(card)
	}
}
*/

class PlayerHand(
	val id: PlayerId,
	val cards: MutableList<Card> = mutableListOf()
) {
	/**
	
	Fügt der Hand eine einzelne Karte hinzu.*/
	fun add(card: Card) {
		cards.add(card)}
}
