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
		val trump = Trump.drawFromDeck(deck)
		val trumpSuit = trump.suit()
		
		// then
		assertThat(trumpSuit).isIn(*Suit.entries.toTypedArray())
		assertThat(deck.cards.size).isEqualTo(initialDeckSize - 1) // One card should be drawn
	}
}