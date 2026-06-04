package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CardCompareTest {
	
	@Test
	fun `CardDefend exists and can be created`() {
		//given
		//when
		val cardDefend = CardDefend()
		//then
		assertThat(cardDefend).isNotNull()
	}
	
	@Test
	fun `CardDefend can hold cards`() {
		//given
		val firstCard = Card(Suit.HEARTS, Rank.KING)
		val secondCard = Card(Suit.SPADES, Rank.QUEEN)
		val cardDefend = CardDefend()
		
		//when
		cardDefend.add(firstCard)
		cardDefend.add(secondCard)
		
		//then
		assertThat(cardDefend.cards()).containsExactly(firstCard, secondCard)
	}
	
	@Test
	fun `CardDefend can be cleared`() {
		//given
		val card = Card(Suit.HEARTS, Rank.KING)
		val cardDefend = CardDefend()
		cardDefend.add(card)
		
		//when
		cardDefend.clear()
		
		//then
		assertThat(cardDefend.cards()).isEmpty()
	}
}