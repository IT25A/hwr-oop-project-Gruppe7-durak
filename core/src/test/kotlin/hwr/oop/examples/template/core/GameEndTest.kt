package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameEndTest {
	
	private val p1 = PlayerId("P1")
	private val p2 = PlayerId("P2")
	private val p3 = PlayerId("P3")
	private val p4 = PlayerId("P4")
	
	@Test
	fun `endRound without active round throws`() {
		val game = Game(emptyMap(), listOf(p1, p2), MutableDeck(mutableListOf()), roundActive = false)
		assertThrows<NoActiveRoundException> { game.endRound() }
	}
	
	@Test
	fun `endRound when defender wins discards both attack and defend and rotates roles`() {
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val defendCard = Card(Suit.SPADES, Rank.SEVEN) // same suit higher
		
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(listOf(defendCard), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val deck = MutableDeck(mutableListOf())
		val game = Game(hands, listOf(p1, p2), deck, roundActive = true)
		
		game.attackWithCard(attackCard)
		game.defendCard(attackCard, defendCard)
		
		game.endRound()
		
		// discard pile should contain two cards (attack + defend)
		assertThat(game.getGameStatus()).contains("Discard pile: 2") // status contains discard count
		// roles rotated
		assertThat(game.getAttacker()).isEqualTo(p2)
	}
	
	@Test
	fun `endRound defender loses with some defended cards - defender takes all including defended ones`() {
		val attack1 = Card(Suit.SPADES, Rank.SIX)
		val attack2 = Card(Suit.CLUBS, Rank.SEVEN)
		val defend1 = Card(Suit.SPADES, Rank.SEVEN) // same suit higher so defend succeeds
		
		val attackerHand = PlayerHand.create(listOf(attack1, attack2), p1)
		val defenderHand =
			PlayerHand.create(listOf(defend1, Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val deck = MutableDeck(mutableListOf())
		val game = Game(hands, listOf(p1, p2), deck, roundActive = true)
		
		game.attackWithCard(attack1)
		// attempt to defend first attack (may or may not succeed depending on card ordering)
		game.defendCard(attack1, defend1)
		
		// attacker plays a second card which defender cannot beat
		game.attackWithCard(attack2)
		
		// end round -> defender loses and must take both attack and defend cards
		game.endRound()
		
		val newDefenderHand = game.getPlayerHand(p2)
		// defender should at least take both attacking cards
		assertThat(newDefenderHand?.cards()).contains(attack1, attack2)
	}
	
	@Test
	fun `endRound defender loses results in defender taking cards`() {
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val deck = MutableDeck(mutableListOf())
		val game = Game(hands, listOf(p1, p2), deck, roundActive = true)
		
		game.attackWithCard(attackCard)
		
		// Defender cannot defend -> endRound should make defender take the card
		game.endRound()
		
		val newDefenderHand = game.getPlayerHand(p2)
		assertThat(newDefenderHand?.cards()).contains(attackCard)
	}
	
	@Test
	fun `game over and loser detection`() {
		val hands = mapOf(
			p1 to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1),
			p2 to PlayerHand.create(emptyList(), p2)
		)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		assertThat(game.isGameOver()).isTrue()
		assertThat(game.getLoser()).isEqualTo(p2)
	}
	
	@Test
	fun `at the end of a round card-pairs and current attackers are cleared`() {
		val attackerHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SEVEN)), p2)
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(
			hands,
			listOf(p1, p2),
			deck,
			roundCardPairings = mutableMapOf(Card(Suit.CLUBS, Rank.SIX) to Card(Suit.CLUBS, Rank.SEVEN)),
			currentRoundAttackers = mutableListOf(p1),
			roundActive = true
		)
		
		
		game.attackWithCard(Card(Suit.CLUBS, Rank.SIX))
		game.endRound()
		
		assertThat(game.getRoundCardPairings()).isEmpty()
		assertThat(game.getCurrentRoundAttackers()).containsExactly(p2)
	}
	
	@Test
	fun `at the end defender lose and the hands will be replenished`() {
		val attackCard = Card(Suit.CLUBS, Rank.SIX)
		val defendCard = Card(Suit.CLUBS, Rank.SEVEN) // beats the attack card
		val joinCard = Card(Suit.HEARTS, Rank.SIX) // same rank as attack, will be undefended
		
		val p1Hand = PlayerHand.create(listOf(attackCard), p1)
		val p2Hand = PlayerHand.create(
			listOf(defendCard, Card(Suit.SPADES, Rank.JACK), Card(Suit.SPADES, Rank.NINE)),
			p2
		) // Only one card - can't defend both attacks
		val p3Hand = PlayerHand.create(emptyList(), p3) // Will receive cards from replenish
		val p4Hand = PlayerHand.create(listOf(joinCard), p4)
		
		val hands = mapOf(p1 to p1Hand, p2 to p2Hand, p3 to p3Hand, p4 to p4Hand)
		val deck = Deck.createRandomDeck().toMutableDeck()
		val game = Game(hands, listOf(p1, p2, p3, p4), deck, roundActive = true)
		
		
		game.attackWithCard(attackCard)
		game.defendCard(attackCard, defendCard)
		game.joinAttack(p4, joinCard)
		
		assertThat(game.hasUndefendedCards()).isTrue()
		
		val p2CardsBeforeLoss = game.getPlayerHand(p2)?.cards()?.size ?: 0
		
		game.endRound()
		
		val p2CardsAfterLoss = game.getPlayerHand(p2)?.cards()?.size ?: 0
		assertThat(p2CardsAfterLoss).isGreaterThan(p2CardsBeforeLoss)
		
		assertThat(game.getAttacker()).isEqualTo(p2) // Previous defender becomes attacker
		assertThat(game.getDefender()).isEqualTo(p3) // Next player becomes defender
		
		assertThat(game.getCurrentRoundAttackers()).contains(p2)
		
		assertThat(game.getPlayerHand(p1)?.cards()?.size ?: 0).isEqualTo(6)
		
	
		assertThat(game.getPlayerHand(p2)?.cards()?.size ?: 0).isGreaterThanOrEqualTo(2)
		
		assertThat(game.getPlayerHand(p3)?.cards()?.size ?: 0).isEqualTo(6)
		
		assertThat(game.getPlayerHand(p4)?.cards()?.size ?: 0).isEqualTo(6)
	}
	
	@Test
	fun `getLoser returns null when game not over`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		assertThat(game.isGameOver()).isFalse()
		assertThat(game.getLoser()).isNull()
	}
}