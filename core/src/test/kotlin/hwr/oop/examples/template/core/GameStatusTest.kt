package hwr.oop.examples.template.core

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class GameStatusTest {
	
	@Test
	fun `both game states exist`(){
		//given
		//when
		val gameStatus = GameStatus.entries.toTypedArray()
		//then
		assertThat(gameStatus).containsExactlyInAnyOrderElementsOf(listOf(GameStatus.IN_PROGRESS, GameStatus.FINISHED))
		
	}
}