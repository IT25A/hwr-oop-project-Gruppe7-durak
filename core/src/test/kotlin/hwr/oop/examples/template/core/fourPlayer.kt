package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class fourPlayer {
	@Test
	fun `Test four players`() {
		//given
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		val player1 = PlayerHand(PlayerId("1"))
		val player2 = PlayerHand(PlayerId("2"))
		val player3 = PlayerHand(PlayerId("3"))
		val player4 = PlayerHand(PlayerId("4"))
		
		//when
		println("Deck size before: ${deck.cards.size}")
		
		deck.dealTo(player1, 6)
		println("Player1 hand size: ${player1.cards.size}")
		
		deck.dealTo(player2, 6)
		println("Player2 hand size: ${player2.cards.size}")
		
		deck.dealTo(player3, 6)
		println("Player3 hand size: ${player3.cards.size}")
		
		deck.dealTo(player4, 6)
		println("Player4 hand size: ${player4.cards.size}")
		
		println("Deck size after: ${deck.cards.size}")
		
		//then
		assertThat(player1.cards.size).isEqualTo(6)
		assertThat(player2.cards.size).isEqualTo(6)
		assertThat(player3.cards.size).isEqualTo(6)
		assertThat(player4.cards.size).isEqualTo(6)
	}
	
	@Test
	fun `Test three players`() {
		//given
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		val player1 = PlayerHand(PlayerId("1"))
		val player2 = PlayerHand(PlayerId("2"))
		val player3 = PlayerHand(PlayerId("3"))
		
		//when
		println("Deck size before: ${deck.cards.size}")
		
		deck.dealTo(player1, 6)
		println("Player1 hand size: ${player1.cards.size}")
		
		deck.dealTo(player2, 6)
		println("Player2 hand size: ${player2.cards.size}")
		
		deck.dealTo(player3, 6)
		println("Player3 hand size: ${player3.cards.size}")
		
		println("Deck size after: ${deck.cards.size}")
		
		//then
		assertThat(player1.cards.size).isEqualTo(6)
		assertThat(player2.cards.size).isEqualTo(6)
		assertThat(player3.cards.size).isEqualTo(6)
	}
}