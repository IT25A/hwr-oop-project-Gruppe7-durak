package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class BasicCardCreationTest {
	
	@Test
	fun `all suits exist`() {
		// given
		val suits = Suit.entries
		// when
		// then
		assertThat(suits).containsExactlyInAnyOrder(
			Suit.SPADES,
			Suit.HEARTS,
			Suit.DIAMONDS,
			Suit.CLUBS
		)
	}
	
	@ParameterizedTest
	@EnumSource(Rank::class)
	fun `all suits, each rank can exist`(rank: Rank) {
		// given
		val allSuits = Suit.entries
		
		// when
		
		//map makes a list of cards with each suit and the rank of the iteration
		val cards = allSuits.map { Card(it, rank) }
		
		// then
		assertThat(cards)
			//asserts that the size of the list is equal to the size of the allSuits list
			.hasSize(allSuits.size)
			//asserts that all cards have the same rank
			.allMatch { it.rank() == rank }
	}
	
	@Test
	fun `all ranks exist`() {
		// given
		val ranks = Rank.entries
		// when
		// then
		assertThat(ranks).containsExactlyInAnyOrder(
			Rank.SIX,
			Rank.SEVEN,
			Rank.EIGHT,
			Rank.NINE,
			Rank.TEN,
			Rank.JACK,
			Rank.QUEEN,
			Rank.KING,
			Rank.ACE
		)
	}
	
	//ParameterizedTest is a JUnit 5 test that can be run multiple times with different input data
	//in this case for every suit in the Suit enum
	@ParameterizedTest
	@EnumSource(Suit::class)
	fun `all ranks, each suit can exist`(suit: Suit) {
		// given
		val allRanks = Rank.entries
		
		// when
		
		//map makes a list of cards with each rank and the suit of the iteration
		val cards = allRanks.map { Card(suit, it) }
		
		// then
		assertThat(cards)
			//asserts that the size of the list is equal to the size of the allRanks list
			.hasSize(allRanks.size)
			//asserts that all cards have the same suit
			.allMatch { it.suit() == suit }
		
		//asserts that every rank in the list is contained in the allRanks list
		val ranks = cards.map { it.rank() }
		assertThat(ranks).containsExactlyInAnyOrderElementsOf(allRanks)
	}
}

