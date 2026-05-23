package hwr.oop.examples.template.core

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