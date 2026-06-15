package hwr.oop.examples.template.core


class AttackStack(vararg initialCards: Card) {
	private val cards: MutableList<Card> = initialCards.toMutableList()
	
	fun add(card: Card) {
		cards.add(card)
	}
	
	fun cards(): List<Card> = cards.toList()
	
	fun clear() {
		cards.clear()
	}
	
	
	val cardlist: List<Card> get() = cards()
}
