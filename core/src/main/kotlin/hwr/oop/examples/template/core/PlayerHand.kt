package hwr.oop.examples.template.core

@JvmInline
value class PlayerId(private val value: String) {

}

data class PlayerHand constructor(
	private val id: PlayerId,
	private val cardsInternal: List<Card>,
) {
	companion object {
		fun create(cards: List<Card> = emptyList(), id: PlayerId = PlayerId("")): PlayerHand {
			return PlayerHand(id, cards.toList())
		}
	}
	
	fun contains(card: Card): Boolean = cardsInternal.contains(card)
	
	
	fun without(card: Card): PlayerHand {
		return PlayerHand(id, cardsInternal.filter { it != card })
	}
	
	
	fun withAdded(cards: Collection<Card>): PlayerHand {
		return PlayerHand(id, cardsInternal + cards)
	}
	
	
	fun cards(): List<Card> = cardsInternal.toList()
	
	fun getId(): PlayerId = id
}
