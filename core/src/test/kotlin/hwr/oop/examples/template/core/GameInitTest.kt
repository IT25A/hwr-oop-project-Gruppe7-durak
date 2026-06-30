package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameInitTest {
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
	
	@Test
	fun `create with 2 players initializes correct roles`() {
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val game = Game.create(listOf(p1, p2))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.getPlayerHand(p1)?.cards()).hasSize(6)
		assertThat(game.getPlayerHand(p2)?.cards()).hasSize(6)
	}
	
	@Test
	fun `create with 3 players initializes correct roles`() {
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val p3 = PlayerId("Player3")
		val game = Game.create(listOf(p1, p2, p3))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.getCurrentRoundAttackers()).contains(p1)
	}
	
	@Test
	fun `create with invalid player numbers throws`() {
		assertThrows<InvalidPlayerNumberException> { Game.create(listOf(PlayerId("P1"))) }
		assertThrows<InvalidPlayerNumberException> { Game.create((1..5).map { PlayerId("P$it") }) }
		assertThrows<InvalidPlayerNumberException> { Game.create(emptyList()) }
	}
	
	@Test
	fun `NoActiveBoutException has correct message`() {
		val exception = NoActiveBoutException()
		
		assertThat(exception).hasMessage("No active bout")
	}
}