package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PlayerCountTest {
	
	@ParameterizedTest
	@ValueSource(ints = [2, 3, 4, 5])
	fun `dealing cards to different numbers of players`(playerCount: Int) {
		// given
		val deck = Deck.createRandomDeck().toMutableDeck()
		val players = (1..playerCount).map { PlayerHand.create(id = PlayerId("Player $it"), cards = listOf()) }
		
		// when
		val initialDeckSize = deck.cards.size
		// deal and collect updated player hands (PlayerHand is immutable)
		val updatedPlayers = players.map { deck.dealTo(it, 6) }
		
		// then
		// Each player should have 6 cards
		updatedPlayers.forEach { player ->
			assertThat(player.cards().size).isEqualTo(6)
		}
		
		// Deck should have decreased by 6 cards per player
		assertThat(deck.cards.size).isEqualTo(initialDeckSize - (playerCount * 6))
	}
}