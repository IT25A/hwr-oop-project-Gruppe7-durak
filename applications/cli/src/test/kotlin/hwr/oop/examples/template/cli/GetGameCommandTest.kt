package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.util.UUID

class GetGameCommandTest : CliCommandTestBase() {
	
	@Test
	fun `getGame loads persisted game`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		rootWithGameId(GetGameCommand(persistence))
			.parse(listOf("onGameID", gameId, "getGame"))
		
		val loaded = persistence.loadGame(gameId)
		assertThat(loaded.getPlayers()).hasSize(2)
	}
	
	@Test
	fun `getGame fails when persistence is not configured`() {
		val gameId = UUID.randomUUID().toString()
		
		assertThatThrownBy {
			rootWithGameId(GetGameCommand(null))
				.parse(listOf("onGameID", gameId, "getGame"))
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
}