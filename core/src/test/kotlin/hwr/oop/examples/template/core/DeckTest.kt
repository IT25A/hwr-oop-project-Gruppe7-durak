package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckTest {

	@Test
	fun `deck can be created with specific cards`() {
		// given
		val cards = listOf(Card(Suit.SPADES, Rank.ACE), Card(Suit.HEARTS, Rank.KING))

		// when
		val deck = Deck(cards)

		// then
		assertThat(deck.cards()).isEqualTo(cards)
	}

	@Test
	fun `cards() returns the deck's cards`() {
		// given
		val expectedCards = listOf(Card(Suit.SPADES, Rank.ACE))

		// when
		val deck = Deck(expectedCards)
		val actualCards = deck.cards()

		// then
		assertThat(actualCards).isEqualTo(expectedCards)
	}

	@Test
	fun `toMutableDeck() converts deck to mutable deck with same cards`() {
		// given
		val cards = listOf(Card(Suit.SPADES, Rank.ACE), Card(Suit.HEARTS, Rank.KING))
		val deck = Deck(cards)

		// when
		val mutableDeck = deck.toMutableDeck()

		// then
		assertThat(mutableDeck.cards).containsExactlyElementsOf(cards)
	}

	@Test
	fun `createRandomDeck() creates deck with 36 cards`() {
		// given
		// when
		val deck = Deck.createRandomDeck()

		// then
		assertThat(deck.cards()).hasSize(36) // 4 suits × 9 ranks = 36 cards
	}

	@Test
	fun `createRandomDeck() contains all suits and ranks`() {
		// given
		// when
		val deck = Deck.createRandomDeck()
		val cards = deck.cards()

		// then
		// Check that all suits are present
		val suits = cards.map { it.suit() }.toSet()
		assertThat(suits).containsExactlyInAnyOrder(*Suit.entries.toTypedArray())

		// Check that all ranks are present
		val ranks = cards.map { it.rank() }.toSet()
		assertThat(ranks).containsExactlyInAnyOrder(*Rank.entries.toTypedArray())

		// Check that each suit-rank combination appears exactly once
		val cardSet = cards.toSet()
		assertThat(cardSet).hasSize(36) // All combinations should be unique
	}

	@Test
	fun `createRandomDeck() returns different order each time (shuffled)`() {
		// given
		val deck1 = Deck.createRandomDeck()
		val deck2 = Deck.createRandomDeck()

		// when
		val cards1 = deck1.cards()
		val cards2 = deck2.cards()

		// then
		// Note: This test could theoretically fail if the shuffle happens to produce the same order,
		// but the probability is extremely low (1 in 36! ≈ 1 in 3.72 × 10^49)
		assertThat(cards1).isNotEqualTo(cards2)
	}
}