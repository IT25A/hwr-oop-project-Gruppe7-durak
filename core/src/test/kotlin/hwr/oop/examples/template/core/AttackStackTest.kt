package hwr.oop.examples.template.core

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple

class AttackStackTest() {
	
	@Test
	fun `Attack Stack can exist`() {
		//given
		//when
		val stack = AttackStack()
		//then
		assertThat(stack).isNotNull()
	}
	
	@Test
	fun `Attack Stack can have multiple cards`() {
		// given
		val cardOne = Card(Suit.HEARTS, Rank.KING)
		val cardTwo = Card(Suit.HEARTS, Rank.JACK)
		val attackStack = AttackStack(cardOne)
		
		// when
		attackStack.add(cardTwo)
		
		// then
		assertThat(attackStack.cardlist)
			.containsExactly(cardOne, cardTwo)
	}
}