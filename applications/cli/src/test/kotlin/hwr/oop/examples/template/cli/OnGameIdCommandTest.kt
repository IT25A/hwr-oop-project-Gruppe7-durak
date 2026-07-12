package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class OnGameIdCommandTest : CliCommandTestBase() {
	
	@Test
	fun `onGameId command passes object to child command`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		rootWithGameId(GetGameCommand(persistence))
			.parse(listOf("onGameID", gameId, "getGame"))
		
		val loaded = persistence.loadGame(gameId)
		assertThat(loaded.getPlayers()).hasSize(2)
	}
	
	@Test
	fun `onGameId command fails without argument`() {
		assertThatThrownBy {
			rootWithGameId(GetGameCommand(persistence))
				.parse(listOf("onGameID"))
		}.isInstanceOf(Exception::class.java)
	}
}