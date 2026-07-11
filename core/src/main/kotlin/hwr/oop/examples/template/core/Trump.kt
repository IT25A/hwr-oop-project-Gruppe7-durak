package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable
class Trump private constructor(
	private val suit: Suit,
) {
	fun suit(): Suit = suit
	
	companion object {
		fun drawFromDeck(deck: MutableDeck): Pair<MutableDeck, Trump> {
			val trumpCard = deck.draw()!!
			deck.cards.add(trumpCard)
			return Pair(deck, Trump(trumpCard.suit()))
		}
		
		fun of(suit: Suit): Trump = Trump(suit)
	}
}

