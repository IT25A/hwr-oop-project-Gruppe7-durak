package hwr.oop.examples.template.core

/*
data class MutableDeck(
	val cards: MutableList<Card>
) {
	fun draw(count: Int): List<Card> {
		val drawn = cards.take(count)
		(1..count).forEach { _ -> cards.removeFirst() }
		return drawn
	}
}
*/

data class MutableDeck(
	val cards: MutableList<Card> = mutableListOf()
) {

	fun draw(): Card? {
		return if (cards.isNotEmpty()) cards.removeAt(0) else null}
	
	fun dealTo(playerHand: PlayerHand, count: Int) {
		repeat(count) {
			val card = draw()
			if (card != null) {
				playerHand.cards.add(card)}
		}
	}
}