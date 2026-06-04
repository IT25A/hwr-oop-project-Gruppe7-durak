package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Arrays;
import java.util.List;

class GameTest {
	
	@Test
	fun `Game can exist`() {
		//given
		//when
		val game = Game(listOf(), listOf()
		)
		//then
		assertThat(game).isNotNull()
	}
}

