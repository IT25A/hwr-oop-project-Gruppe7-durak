package hwr.oop.examples.template.core

data class Card(
	private val suit: Suit,
	private val rank: Rank,
) {
	
	fun suit(): Suit = suit
	fun rank(): Rank = rank
	
	fun getCardValue(rank: Rank): Int {
		val cardValues = mapOf(
			"2" to 2, "3" to 3, "4" to 4, "5" to 5,
			"6" to 6, "7" to 7, "8" to 8, "9" to 9,
			"10" to 10, "J" to 11, "Q" to 12, "K" to 13, "A" to 14
		)
		// Gibt den Wert zurück, oder 0, wenn der Rang nicht gefunden wurde
		return cardValues[rank] ?: 0
	}
}
