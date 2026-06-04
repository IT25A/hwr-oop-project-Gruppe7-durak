package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GameTest {
	
	@Test
	fun `Game can exist`() {
		//given
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val handsOfPlayers = mapOf(
			p1 to PlayerHand.create(id = p1),
			p2 to PlayerHand.create(id = p2)
		)
		val players = listOf(p1, p2)
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		//when
		val game = Game(handsOfPlayers, players, deck)
		
		//then
		assertThat(game).isNotNull()
	}
}
