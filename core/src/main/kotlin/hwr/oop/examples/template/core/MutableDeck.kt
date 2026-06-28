package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable
data class MutableDeck(
	val cards: MutableList<Card> = mutableListOf(),
) {
	
	fun draw(): Card? {
		return if (cards.isNotEmpty()) cards.removeAt(0) else null
	}
	
	fun dealTo(playerHand: PlayerHand, count: Int): PlayerHand {
		
		var newPlayerHand = playerHand
		repeat(count) {
			val card = draw()
			if (card != null) {
				newPlayerHand = newPlayerHand.withAdded(listOf(card))
			}
		}
		return newPlayerHand
	}
}

