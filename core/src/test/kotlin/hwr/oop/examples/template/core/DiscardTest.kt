package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DiscardTest {
	
	@Test
	fun `check if single cards can be added`(){
		// given
		val pilecards = Card(Suit.SPADES , Rank.QUEEN)
		
		// when
		val discardPile = DiscardPile()
		discardPile.add(pilecards)
		
		// then
		assertThat(discardPile.cards()).containsExactly(pilecards)
	}
}
