package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameErrorTest {
	
	private val p1 = PlayerId("P1")
	private val p2 = PlayerId("P2")
	private val p3 = PlayerId("P3")
	private val attacker = PlayerId("Attacker")
	private val defender = PlayerId("Defender")
	
	private val spade6 = Card(Suit.SPADES, Rank.SIX)
	private val heart7 = Card(Suit.HEARTS, Rank.SEVEN)
	private val club6 = Card(Suit.CLUBS, Rank.SIX)
	
	@Test
	fun `cannot attack or defend with card not in hand`() {
		val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(listOf(heart7), p2))
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck()).apply { startRound() }
		
		assertThrows<AttackerDoesNotHaveCardException> { game.attackWithCard(club6) }
		assertThrows<AttackStackDoesNotContainCardException> { game.defendCard(Card(Suit.CLUBS, Rank.SEVEN), club6) }
	}
	
	@Test
	fun `attackWithCard throws AttackerNotFoundException when attacker is missing`() {
		val hands = mapOf(defender to PlayerHand.create(emptyList(), defender))
		val game = Game(hands, listOf(attacker, defender), MutableDeck(mutableListOf())).apply { startRound() }
		
		val ex = assertThrows<AttackerNotFoundException> { game.attackWithCard(spade6) }
		assertThat(ex.message).isEqualTo("Attacker not found")
	}
	
	@Test
	fun `defendCard throws DefenderNotFoundException when defender is missing`() {
		val hands = mapOf(attacker to PlayerHand.create(listOf(spade6), attacker))
		val game = Game(hands, listOf(attacker, defender), MutableDeck(mutableListOf())).apply { startRound() }.apply { attackWithCard(spade6) }
		
		val ex = assertThrows<DefenderNotFoundException> { game.defendCard(spade6, heart7) }
		assertThat(ex.message).isEqualTo("Defender not found")
	}
	
	@Test
	fun `defendCard throws when attacking card not in round`() {
		val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(listOf(heart7), p2))
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck()).apply { startRound() }
		
		assertThrows<AttackStackDoesNotContainCardException> { game.defendCard(spade6, heart7) }
	}
	
	@Test
	fun `defendCard without active round throws`() {
		val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(listOf(heart7), p2))
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		assertThrows<NoActiveRoundException> { game.defendCard(spade6, heart7) }
	}
	
	@Test
	fun `defendCard throws when defender does not have the card`() {
		val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(listOf(heart7), p2))
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck()).apply { startRound() }.apply { attackWithCard(spade6) }
		
		assertThrows<DefenderDoesNotHaveCardException> { game.defendCard(spade6, Card(Suit.CLUBS, Rank.NINE)) }
	}
	
	@Test
	fun `throws when not first attack and rank not on table and attackStackSize equals defenderCards`() {
		val secondAttack = Card(Suit.CLUBS, Rank.SEVEN)
		val hands = mapOf(
			attacker to PlayerHand.create(listOf(spade6, secondAttack), attacker),
			defender to PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.KING)), defender)
		)
		val game = Game(hands, listOf(attacker, defender), Deck.createRandomDeck().toMutableDeck()).apply { startRound() }
		assertThat(game.attackWithCard(spade6)).isTrue
		
		Assertions.assertThrows(RankNotOnTableException::class.java) { game.attackWithCard(secondAttack) }
	}
	
	@Test
	fun `joinAttack throws DefenderDoesNotHaveEnoughCardsException when attacker plays more cards than defender holds`() {
		val ap1 = PlayerId("Attacker-P1")
		val dp2 = PlayerId("Defender-P2")
		val jp3 = PlayerId("Joiner-P3")
		val hands = mapOf(
			ap1 to PlayerHand.create(listOf(spade6), ap1),
			dp2 to PlayerHand.create(emptyList(), dp2),
			jp3 to PlayerHand.create(listOf(club6), jp3)
		)
		val game = Game(hands, listOf(ap1, dp2, jp3), MutableDeck(mutableListOf())).apply { startRound() }.apply { attackWithCard(spade6) }
		
		assertThrows<DefenderDoesNotHaveEnoughCardsException> { game.joinAttack(jp3, club6) }
	}

	@Test
	fun `attackWithCard throws NoActiveBoutException when round is active but bout is missing`() {
		val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1))
		val game = Game(hands, listOf(p1, p2, p3), MutableDeck(mutableListOf()), roundActive = true, currentBout = null)
		
		assertThrows<NoActiveBoutException> { game.attackWithCard(spade6) }
	}
	
	@Test
	fun `joinAttack throws NoActiveBoutException when round is active but bout is missing`() {
		val hands = mapOf(p3 to PlayerHand.create(listOf(club6), p3))
		val game = Game(hands, listOf(p1, p2, p3), MutableDeck(mutableListOf()), roundActive = true, currentBout = null)
		
		assertThrows<NoActiveBoutException> { game.joinAttack(p3, club6) }
	}
	
	@Test
	fun `defendCard throws NoActiveBoutException when round is active but bout is missing`() {
		val hands = mapOf(p2 to PlayerHand.create(listOf(heart7), p2))
		val game = Game(hands, listOf(p1, p2, p3), MutableDeck(mutableListOf()), roundActive = true, currentBout = null)
		
		assertThrows<NoActiveBoutException> { game.defendCard(spade6, heart7) }
	}
	
	@Test
	fun `endRound throws NoActiveBoutException when round is active but bout is missing`() {
		val game = Game(emptyMap(), listOf(p1, p2, p3), MutableDeck(mutableListOf()), roundActive = true, currentBout = null)
		
		assertThrows<NoActiveBoutException> { game.endRound() }
	}
}
