package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class StartGameCommandTest : CliCommandTestBase() {
	
	@Test
	fun `startGame with two players succeeds`() {
		rootWith(StartGameCommand(persistence))
			.parse(listOf("startGame", "--player-id", "p1", "--player-id", "p2"))
	}
	
	@Test
	fun `startGame with three players succeeds`() {
		rootWith(StartGameCommand(persistence))
			.parse(listOf("startGame", "--player-id", "p1", "--player-id", "p2", "--player-id", "p3"))
	}
	
	@Test
	fun `startGame fails when persistence is not configured`() {
		assertThatThrownBy {
			rootWith(StartGameCommand(null))
				.parse(listOf("startGame", "--player-id", "p1", "--player-id", "p2"))
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `startGame fails with one player`() {
		assertThatThrownBy {
			rootWith(StartGameCommand(persistence))
				.parse(listOf("startGame", "--player-id", "p1"))
		}.isInstanceOf(Exception::class.java)
	}
	
	@Test
	fun `startGame fails with seven players`() {
		assertThatThrownBy {
			rootWith(StartGameCommand(persistence)).parse(
				listOf(
					"startGame",
					"--player-id", "p1",
					"--player-id", "p2",
					"--player-id", "p3",
					"--player-id", "p4",
					"--player-id", "p5",
					"--player-id", "p6",
					"--player-id", "p7"
				)
			)
		}.isInstanceOf(Exception::class.java)
	}
}