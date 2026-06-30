package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable
class CardDefend {
	private val cards: MutableList<Card> = mutableListOf()
	
	fun add(card: Card) {
		cards.add(card)
	}
	
	fun cards(): List<Card> = cards.toList()
	
	fun clear() {
		cards.clear()
	}
}
