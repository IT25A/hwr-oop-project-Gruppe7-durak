package hwr.oop.examples.template.core

import org.junit.jupiter.api.Test

class DealTest {
	@Test
	fun `check if cards are actually removed`()
	{
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		val card = deck.draw()
		
		println("Size after one draw: ${deck.cards.size}")
		
		assert(deck.cards.size == 35)
	}
	
	@Test
	fun `Test player hand`() {
		val deck = Deck.createRandomDeck().toMutableDeck()
		val playerHand = PlayerHand(PlayerId("test"))
		
		println("Deck size before: ${deck.cards.size}")
		
		deck.dealTo(playerHand, 6)
		
		println("Deck size after: ${deck.cards.size}")
		println("Player hand size: ${playerHand.cards.size}")
		
		assert(playerHand.cards.size == 6)
	}
}
