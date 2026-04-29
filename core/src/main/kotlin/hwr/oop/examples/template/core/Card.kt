package hwr.oop.examples.template.core

data class Card(
	private val suit: Suit,
	private val rank: Rank,
) {
	fun suit(): Suit = suit
	fun rank(): Rank = rank
}
