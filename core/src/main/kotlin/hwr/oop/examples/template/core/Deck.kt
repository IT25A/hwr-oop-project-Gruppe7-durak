package hwr.oop.examples.template.core

data class Deck(
	private val cards: List<Card>,
) {
	fun cards(): List<Card> = cards
	
	fun toMutableDeck(): MutableDeck = MutableDeck(cards.toMutableList())
	
	companion object {
		fun createRandomDeck(): Deck = Deck(
			Suit.entries.flatMap { suit ->
				Rank.entries.map { rank -> Card(suit, rank) }
			}.shuffled()
		)
	}
}
