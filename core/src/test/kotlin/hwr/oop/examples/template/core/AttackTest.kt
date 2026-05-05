package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AttackTest {
	
	@Test
	fun `first card´s rank is higher, first card wins`() {
		//given
		val firstCard = Card(Suit.HEARTS,Rank.KING)
		val secondCard = Card(Suit.HEARTS,Rank.JACK)
		//when
		val result = Bout.attack(firstCard, secondCard)
		
		//then
		assertThat(result).isEqualTo(firstCard)
	}
	
}