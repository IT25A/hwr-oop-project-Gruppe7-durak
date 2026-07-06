package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GameSupplyPassTest {

	private val p1 = PlayerId("P1")
	private val p2 = PlayerId("P2")
	private val p3 = PlayerId("P3")

	@Test
	fun `create exposes players deck trump and passes`() {
		val game = Game.create(listOf(p1, p2, p3))

		assertThat(game.getPlayers()).containsExactly(p1, p2, p3)
		assertThat(game.getTrumpSuit()).isEqualTo(Suit.HEARTS)
		assertThat(game.getSupplyPasses()).isEmpty()
		assertThat(game.getDeckCards()).hasSize(18)
		assertThat(p1.asString()).isEqualTo("P1")
	}

	@Test
	fun `supplyCard by attacker plays attack and clears passes`() {
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val joinCard = Card(Suit.CLUBS, Rank.SIX)
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		val joinerHand = PlayerHand.create(listOf(joinCard), p3)
		val game = Game(
			mapOf(p1 to attackerHand, p2 to defenderHand, p3 to joinerHand),
			listOf(p1, p2, p3),
			MutableDeck(mutableListOf()),
			roundActive = true
		)

		game.passSupply(p3)
		game.supplyCard(p1, attackCard)

		assertThat(game.getRoundCardPairings()).containsEntry(attackCard, null)
		assertThat(game.getPlayerHand(p1)?.cards()).doesNotContain(attackCard)
		assertThat(game.getSupplyPasses()).isEmpty()
	}

	@Test
	fun `supplyCard by joiner delegates to join attack and clears passes`() {
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val joinCard = Card(Suit.CLUBS, Rank.SIX)
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT)), p2)
		val joinerHand = PlayerHand.create(listOf(joinCard), p3)
		val game = Game(
			mapOf(p1 to attackerHand, p2 to defenderHand, p3 to joinerHand),
			listOf(p1, p2, p3),
			MutableDeck(mutableListOf()),
			roundActive = true
		)

		game.attackWithCard(attackCard)
		game.passSupply(p1)
		game.supplyCard(p3, joinCard)

		assertThat(game.getRoundCardPairings()).containsEntry(joinCard, null)
		assertThat(game.getPlayerHand(p3)?.cards()).doesNotContain(joinCard)
		assertThat(game.getSupplyPasses()).isEmpty()
	}

	@Test
	fun `passSupply ends round when every non defender passed`() {
		val game = Game(
			mapOf(
				p1 to PlayerHand.create(emptyList(), p1),
				p2 to PlayerHand.create(emptyList(), p2),
				p3 to PlayerHand.create(emptyList(), p3),
			),
			listOf(p1, p2, p3),
			MutableDeck(mutableListOf()),
			roundActive = true
		)

		game.passSupply(p1)
		assertThat(game.getSupplyPasses()).containsExactly(p1)

		game.passSupply(p3)

		assertThat(game.getSupplyPasses()).isEmpty()
		assertThat(game.getAttacker()).isEqualTo(p2)
		assertThat(game.getDefender()).isEqualTo(p3)
	}

	@Test
	fun `passSupply rejects defender and duplicate passes`() {
		val game = Game(
			mapOf(
				p1 to PlayerHand.create(emptyList(), p1),
				p2 to PlayerHand.create(emptyList(), p2),
				p3 to PlayerHand.create(emptyList(), p3),
			),
			listOf(p1, p2, p3),
			MutableDeck(mutableListOf()),
			roundActive = true
		)

		game.passSupply(p1)
		assertThrows(PlayerAlreadyPassedSupplyException::class.java) { game.passSupply(p1) }
		assertThrows(DefenderCanNotPassException::class.java) { game.passSupply(p2) }
	}

	@Test
	fun `passSupply rejects inactive rounds and unknown players`() {
		val inactiveGame = Game(
			mapOf(
				p1 to PlayerHand.create(emptyList(), p1),
				p2 to PlayerHand.create(emptyList(), p2),
			),
			listOf(p1, p2),
			MutableDeck(mutableListOf()),
			roundActive = false
		)

		assertThrows(NoActiveRoundException::class.java) { inactiveGame.passSupply(p1) }

		val activeGame = Game(
			mapOf(
				p1 to PlayerHand.create(emptyList(), p1),
				p2 to PlayerHand.create(emptyList(), p2),
				p3 to PlayerHand.create(emptyList(), p3),
			),
			listOf(p1, p2, p3),
			MutableDeck(mutableListOf()),
			roundActive = true
		)

		assertThrows(JoinerNotFoundException::class.java) { activeGame.passSupply(PlayerId("Missing")) }
	}
}
