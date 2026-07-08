package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import hwr.oop.examples.template.core.PlayerId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class SupplyCommandTest : CliCommandTestBase() {
	
	@Test
	fun `supply command as attacker changes table`() {
		val gameId = saveGame(listOf("p1", "p2"))
		val attacker = persistence.loadGame(gameId).getAttacker().asString()
		val attackerCard = persistence.loadGame(gameId)
			.getPlayerHand(PlayerId(attacker))!!
			.cards()
			.first()
		
		rootWithGameId(SupplyCommand(persistence)).parse(
			listOf(
				"onGameID", gameId,
				"supply",
				"--player-id", attacker,
				"--card", "${attackerCard.suit()}:${attackerCard.rank()}"
			)
		)
		
		val reloaded = persistence.loadGame(gameId)
		assertThat(reloaded.getRoundCardPairings()).isNotEmpty
	}
	
	@Test
	fun `supply command fails when persistence is not configured`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		assertThatThrownBy {
			rootWithGameId(SupplyCommand(null)).parse(
				listOf("onGameID", gameId, "supply", "--player-id", "p1", "--card", "HEARTS:ACE")
			)
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `supply command fails on invalid card`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		assertThatThrownBy {
			rootWithGameId(SupplyCommand(persistence)).parse(
				listOf("onGameID", gameId, "supply", "--player-id", "p1", "--card", "INVALID")
			)
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
}