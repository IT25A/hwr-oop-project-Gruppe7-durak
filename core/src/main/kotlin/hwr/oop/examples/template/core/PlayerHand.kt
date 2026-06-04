package hwr.oop.examples.template.core

@JvmInline
value class PlayerId(private val value: String) {

 }

data class PlayerHand constructor(
	private val id: PlayerId,
	private val cardsInternal: List<Card>
) {
	companion object {
		fun create(cards: List<Card> = emptyList(), id: PlayerId = PlayerId("")): PlayerHand {
			return PlayerHand(id, cards.toList())
		}
	}
	
	fun contains(card: Card): Boolean = cardsInternal.contains(card)
	
	// returns a new PlayerHand without the card
	fun without(card: Card): PlayerHand {
		return PlayerHand(id, cardsInternal.filter { it != card })
	}
	
	// returns a new PlayerHand with added cards
	fun withAdded(cards: Collection<Card>): PlayerHand {
		return PlayerHand(id, cardsInternal + cards)
	}
	
	// read-only view for tests / callers
	fun cards(): List<Card> = cardsInternal.toList()
	
	fun getId(): PlayerId = id
}