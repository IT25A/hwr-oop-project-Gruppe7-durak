package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GamePersistence
import hwr.oop.examples.template.core.PlayerId
import hwr.oop.examples.template.core.PlayerHand
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class GetPlayerHandCommandTest : CliCommandTestBase() {
	
	private fun captureOut(block: () -> Unit): String {
		val outputStream = ByteArrayOutputStream()
		val oldOut = System.out
		System.setOut(PrintStream(outputStream))
		try {
			block()
			return outputStream.toString()
		} finally {
			System.setOut(oldOut)
		}
	}
	
	@Test
	fun `getPlayerHand executes full flow successfully with cards formatted`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		val output = captureOut {
			rootWithGameId(GetPlayerHandCommand(persistence))
				.parse(listOf("onGameID", gameId, "getPlayerHand", "--player-id", "p1"))
		}
		
		assertThat(output).contains("=== PLAYER HAND ===", "Player: p1", "Cards (6):")
		assertThat(output.lines().count { it.trim().matches(Regex("""[A-Z]+:[A-Z]+""")) }).isEqualTo(6)
	}
	
	@Test
	fun `getPlayerHand fails when persistence is not configured`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		assertThatThrownBy {
			rootWithGameId(GetPlayerHandCommand(null))
				.parse(listOf("onGameID", gameId, "getPlayerHand", "--player-id", "p1"))
		}.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessage("Persistence is not configured")
	}
	
	@Test
	fun `getPlayerHand handles non-existent player gracefully`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		val output = captureOut {
			rootWithGameId(GetPlayerHandCommand(persistence))
				.parse(listOf("onGameID", gameId, "getPlayerHand", "--player-id", "nonexistent"))
		}
		
		assertThat(output).contains("Player nonexistent not found")
	}
	
	@Test
	fun `getPlayerHand handles empty hand case`() {
		val gameId = saveGame(listOf("p1", "p2"))
		val mockPersistence = mockk<GamePersistence>()
		val mockGame = mockk<Game>()
		val mockEmptyHand = mockk<PlayerHand>()
		
		every { mockPersistence.loadGame(gameId) } returns mockGame
		every { mockGame.getPlayerHand(PlayerId("p1")) } returns mockEmptyHand
		every { mockEmptyHand.cards() } returns emptyList()
		
		val output = captureOut {
			rootWithGameId(GetPlayerHandCommand(mockPersistence))
				.parse(listOf("onGameID", gameId, "getPlayerHand", "--player-id", "p1"))
		}
		
		assertThat(output).contains("Player p1 has no cards")
	}
}