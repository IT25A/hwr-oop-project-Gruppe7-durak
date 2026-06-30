package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable

class AttackStack(
	private val cards: MutableList<Card> = mutableListOf()
) {
	constructor(vararg initialCards: Card) : this(initialCards.toMutableList())
	
	fun add(card: Card) {
		cards.add(card)
	}
	
	fun cards(): List<Card> = cards.toList()
	
	fun clear() {
		cards.clear()
	}
	
	
	val cardlist: List<Card> get() = cards()
}
