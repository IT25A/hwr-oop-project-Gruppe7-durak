package hwr.oop.examples.template.core

class DiscardPile {
	private val cards: MutableList<Card> = mutableListOf()
	
	fun add(card: Card) {
		cards.add(card)
	}
	
	fun addAll(newCards: Collection<Card>) {
		cards.addAll(newCards)
	}
	
	fun cards(): List<Card> = cards.toList()
	
}

