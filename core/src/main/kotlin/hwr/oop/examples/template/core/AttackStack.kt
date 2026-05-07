package hwr.oop.examples.template.core

class AttackStack(vararg initialCards: Card) {
	// Internal storage for cards
	private val cards: MutableList<Card> = initialCards.toMutableList()
	
	// Public read-only access to cards for testing and game logic
	val cardlist: List<Card> get() = cards
	
	fun add(card: Card) {
		cards.add(card)
	}
}