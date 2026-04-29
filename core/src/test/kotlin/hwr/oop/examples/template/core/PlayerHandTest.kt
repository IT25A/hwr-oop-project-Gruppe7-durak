package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerHandTest {
	
	@Test fun `player hand can have an ID`() {
		// given
		
		// when
		val id= PlayerId("Alpha")
		// then
		assertThat(id).isEqualTo(PlayerId("Alpha"))
	}
}
