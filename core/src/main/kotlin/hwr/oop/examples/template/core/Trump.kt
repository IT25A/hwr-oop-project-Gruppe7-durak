package hwr.oop.examples.template.core

class Trump private constructor(
	private val suit: Suit,
) {
	fun suit(): Suit = suit
	
	companion object {
		fun drawFromDeck(deck: MutableDeck): Pair<MutableDeck, Trump> {
			val trumpCard = deck.draw()!!
			deck.cards.add(trumpCard) // Karte ans Ende legen
			return Pair(deck, Trump(trumpCard.suit()))
		}
	}
}
