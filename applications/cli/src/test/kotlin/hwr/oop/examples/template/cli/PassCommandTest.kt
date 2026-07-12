package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import hwr.oop.examples.template.core.PlayerId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PassCommandTest : CliCommandTestBase() {
	
	@Test
	fun `pass command saves modified game`() {
		val gameId = saveGame(listOf("p1", "p2", "p3"))
		val before = persistence.loadGame(gameId).getSupplyPasses()
		
		rootWithGameId(PassCommand(persistence))
			.parse(listOf("onGameID", gameId, "pass", "--player-id", "p1"))
		
		val after = persistence.loadGame(gameId).getSupplyPasses()
		
		assertThat(before).doesNotContain(PlayerId("p1"))
		assertThat(after).contains(PlayerId("p1"))
	}
	
	@Test
	fun `pass command fails when persistence is not configured`() {
		val gameId = saveGame(listOf("p1", "p2", "p3"))
		
		assertThatThrownBy {
			rootWithGameId(PassCommand(null))
				.parse(listOf("onGameID", gameId, "pass", "--player-id", "p1"))
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `pass command fails for defender`() {
		val gameId = saveGame(listOf("p1", "p2", "p3"))
		val defender = persistence.loadGame(gameId).getDefender().asString()
		
		assertThatThrownBy {
			rootWithGameId(PassCommand(persistence))
				.parse(listOf("onGameID", gameId, "pass", "--player-id", defender))
		}.isInstanceOf(Exception::class.java)
	}
}