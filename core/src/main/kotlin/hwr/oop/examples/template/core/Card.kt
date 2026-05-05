package hwr.oop.examples.template.core

data class Card(
	private val suit: Suit,
	private val rank: Rank,
) {
	
	fun suit(): Suit = suit
	fun rank(): Rank = rank
	
	fun getCardValue(rank: Rank): Int {
		val cardValues = mapOf(
			Rank.SIX to 6, Rank.SEVEN to 7, Rank.EIGHT to 8, Rank.NINE to 9,
			Rank.TEN to 10, Rank.JACK to 11, Rank.QUEEN to 12, Rank.KING to 13, Rank.ACE to 14
		)
		return cardValues[rank] ?: 0
	}
}
