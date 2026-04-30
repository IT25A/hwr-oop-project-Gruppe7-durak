package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerHandTest {
	/*
	@Test fun `player hand can have an ID`() {
		// given
		val size = 6
		// when
		val id = PlayerId("Alpha")
		val cards = MutableDeck(deck).draw(size)
		// then
		assertThat(id).isEqualTo(PlayerId("Alpha"))
		assertThat(cards).isEqualTo()
	}
	
}
*/
	
	@Test
	fun `testplayer hand receives cards from mutable deck`() {
		
		val deck = Deck.createRandomDeck().toMutableDeck()
		val playerHand = PlayerHand(PlayerId("test"), )
		val amountToDeal = 6
		
		// 2. Aktion: Die Hand erhält Karten über das Deck
		deck.dealTo(playerHand, amountToDeal)
		
		// 3. Assertions
		
		// Überprüfen, ob die Hand jetzt 6 Karten hat
		assertThat(playerHand.cards.size).isEqualTo(amountToDeal)
		
		// Überprüfen, ob die Karten in der Hand Instanzen der Klasse Card sind
		assertThat(playerHand.cards.all { it is Card }).isEqualTo(true)
		assertThat(playerHand.cards).contains(
			Card(Suit.SPADES, Rank.JACK)
		)
	}
}