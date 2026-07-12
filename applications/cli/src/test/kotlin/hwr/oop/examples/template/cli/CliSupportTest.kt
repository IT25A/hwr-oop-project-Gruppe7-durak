package hwr.oop.examples.template.cli

import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.PlayerId
import hwr.oop.examples.template.core.Rank
import hwr.oop.examples.template.core.Suit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class CliSupportTest {
	
	@Test
	fun `parseCard parses valid card`() {
		val card = parseCard("HEARTS:ACE")
		
		assertThat(card.suit()).isEqualTo(Suit.HEARTS)
		assertThat(card.rank()).isEqualTo(Rank.ACE)
	}
	
	@Test
	fun `parseCard parses another valid card`() {
		val card = parseCard("SPADES:TEN")
		
		assertThat(card.suit()).isEqualTo(Suit.SPADES)
		assertThat(card.rank()).isEqualTo(Rank.TEN)
	}
	
	@Test
	fun `parseCard fails when separator is missing`() {
		assertThatThrownBy {
			parseCard("HEARTS")
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `parseCard fails on invalid suit`() {
		assertThatThrownBy {
			parseCard("INVALID:ACE")
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `parseCard fails on invalid rank`() {
		assertThatThrownBy {
			parseCard("HEARTS:INVALID")
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `printState can be called`() {
		val game = Game.create(listOf(PlayerId("p1"), PlayerId("p2")))
		
		game.printState()
		
		assertThat(game.getGameStatus()).contains("DURAK GAME STATUS")
	}
}