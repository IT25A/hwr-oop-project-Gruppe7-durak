package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CardCompareTest {
	
	@Test
	fun `CardDefend exists`(){
		//given
		//when
		val cardDefend = CardDefend()
		//then
		assertThat(cardDefend).isNotNull()
	}
	
	@Test
	fun`CardDefend can hold two cards`(){
		//given
		val firstCard = Card(Suit.HEARTS, Rank.KING)
		val secondCard = Card(Suit.SPADES, Rank.QUEEN)
		//when
		CardDefend.compareCards(firstCard, secondCard)
		//then
		assertEquals(firstCard, CardDefend.firstCard)
		assertEquals(secondCard, CardDefend.secondCard)
	}
	@Test
	fun `first card's rank is higher, first card wins`() {
		//given
		val firstCard = Card(Suit.HEARTS, Rank.KING)
		val secondCard = Card(Suit.HEARTS, Rank.JACK)
		//when
		val result = CardDefend.compareCards(firstCard, secondCard)
		
		//then
		assertThat(result).isEqualTo(firstCard)
	}
	
	@Test
	fun `second card's rank is higher, second card wins`() {
		//given
		val firstCard = Card(Suit.HEARTS, Rank.JACK)
		val secondCard = Card(Suit.HEARTS, Rank.KING)
		//when
		val result = CardDefend.compareCards(firstCard, secondCard)
		
		//then
		assertThat(result).isEqualTo(secondCard)
	}
	
	@Test
	fun `both card's rank is equal, we get an exception`() {
		//given
		val firstCard = Card(Suit.HEARTS, Rank.KING)
		val secondCard = Card(Suit.SPADES, Rank.KING)
		
		//when/then
		val exception = assertThrows<Exception> {
			CardDefend.compareCards(firstCard, secondCard)
		}
		assertThat(exception.message).isEqualTo("Trump is not implemented yet")
	}
}