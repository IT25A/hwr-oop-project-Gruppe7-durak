package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameRoundTest {
	
	@Test
	fun `getPlayerHand returns null for non-existent player`() {
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val game = Game.create(listOf(p1, p2))
		
		val hand = game.getPlayerHand(PlayerId("Nope"))
		assertThat(hand).isNull()
	}
	
	@Test
	fun `isRoundActive initially false`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		assertThat(game.isRoundActive()).isFalse()
	}
	
	@Test
	fun `round can be started and ended`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		assertThat(game.isRoundActive()).isTrue()
		game.endRound()
		assertThat(game.isRoundActive()).isFalse()
	}
	
	@Test
	fun `cannot start round twice`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		assertThrows<IllegalStateException> { game.startRound() }
	}
	
	@Test
	fun `attackWithCard without active round throws`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val hands = mapOf(
			p1 to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1),
			p2 to PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		val attackCard = hands[p1]?.cards()?.first() ?: return
		assertThrows<NoActiveRoundException> { game.attackWithCard(attackCard) }
	}
	
	@Test
	fun `at the start of a round card-pairs and current attackers are cleared`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val attackerHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SEVEN)), p2)
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(
			hands,
			listOf(p1, p2),
			deck,
			roundCardPairings = mutableMapOf(Card(Suit.CLUBS, Rank.SIX) to Card(Suit.CLUBS, Rank.SEVEN)),
			currentRoundAttackers = mutableListOf(p1)
		)
		
		
		game.startRound()
		
		assertThat(game.getRoundCardPairings()).isEmpty()
		assertThat(game.getCurrentRoundAttackers()).containsExactly(p1)
	}
}