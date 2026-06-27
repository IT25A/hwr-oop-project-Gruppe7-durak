package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GameRotateTest {
	@Test
	fun `roles rotate when round ends with 2 players`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		
		game.endRound()
		
		assertThat(game.getAttacker()).isEqualTo(p2)
		assertThat(game.getDefender()).isEqualTo(p1)
	}
	
	@Test
	fun `roles rotate through 3 players and return`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		
		val sequence = mutableListOf<Pair<PlayerId, PlayerId>>()
		sequence.add(game.getAttacker() to game.getDefender())
		
		repeat(3) {
			game.endRound()
			sequence.add(game.getAttacker() to game.getDefender())
		}
		
		assertThat(sequence).hasSize(4)
		assertThat(sequence.last().first).isEqualTo(sequence.first().first)
	}
}