package hwr.oop.examples.template.core

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class GameStateTest {
	
	@Test
	fun `both game states exist`(){
		//given
		//when
		val gameState = GameState.entries.toTypedArray()
		//then
		assertThat(gameState).containsExactlyInAnyOrderElementsOf(listOf(GameState.IN_PROGRESS, GameState.FINISHED))
		
	}
}