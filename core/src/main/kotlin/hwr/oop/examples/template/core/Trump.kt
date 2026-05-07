package hwr.oop.examples.template.core

class Trump private constructor(
	private val suit: Suit,
) {
	fun suit(): Suit = suit
	
	companion object {
		fun drawFromDeck(deck: MutableDeck): Trump {
			val trumpCard = deck.draw()!!
			return Trump(trumpCard.suit())
		}
	}
}
