package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TrumpTest {
	
	@Test
	fun `trump suit is drawn from deck and is valid`() {
		// given
		val deck = Deck.createRandomDeck().toMutableDeck()
		val initialDeckSize = deck.cards.size
		
		// when
		val (updatedDeck, trump) = Trump.drawFromDeck(deck)
		val trumpSuit = trump.suit()
		
		// then
		assertThat(trumpSuit).isIn(*Suit.entries.toTypedArray())
		assertThat(updatedDeck.cards.size).isEqualTo(initialDeckSize) // Deck size unchanged (card moved to end)
	}
	
	@Test
	fun `trump card is put to the end of the deck `() {
		// given
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		// when
		val originalFirstCard = deck.cards.first()
		val (updatedDeck, trumpSuit) = Trump.drawFromDeck(deck)
		val finalLastCard = updatedDeck.cards.last()
		
		// then
		assertThat(originalFirstCard).isEqualTo(finalLastCard)
	}
	
	@Test
	fun `drawFromDeck on empty deck does not modify deck`() {
		// given: ein leeres MutableDeck, sodass draw() null zurückgibt
		val deck = MutableDeck(mutableListOf())
		
		// when / then: erwartet wird eine NullPointerException
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException::class.java) {
			Trump.drawFromDeck(deck)
		}
		
		// danach: das Deck darf nicht verändert worden sein (original: leer)
		assertThat(deck.cards).isEmpty()
	}
}