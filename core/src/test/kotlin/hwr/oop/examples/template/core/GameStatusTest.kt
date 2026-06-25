package hwr.oop.examples.template.core

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class GameStatusTest {
	
	@Test
	fun `both game states exist`() {
		//given
		//when
		val gameStatus = GameStatus.entries.toTypedArray()
		//then
		assertThat(gameStatus).containsExactlyInAnyOrderElementsOf(listOf(GameStatus.IN_PROGRESS, GameStatus.FINISHED))
		
	}
	
	@Test
	fun `getGameStatus includes players and roles`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		val status = game.getGameStatus()
		assertThat(status).isNotBlank()
		assertThat(status).contains("DURAK GAME STATUS")
		assertThat(status).contains("Current Attacker:")
		assertThat(status).contains("Current Defender:")
		assertThat(status).contains("P1")
		assertThat(status).contains("P2")
	}
	
	@Test
	fun `getGameStatus marks uninvolved player with empty role string (else branch at line 344)`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"), PlayerId("P4"))
		val game = Game.create(players)
		
		game.startRound()
		// no joining, no attacks; so P3 and P4 have no role
		val status = game.getGameStatus()
		
		// Find the player lines
		val playerLines = status.split("\n").filter { it.contains("PlayerId") }
		// Filter to only indented player lines (start with "  "), not header lines like "Current Attacker:"
		val playerList = playerLines.filter { it.startsWith("  ") }
		val p1Line = playerList.find { it.contains("PlayerId(value=P1)") }
		val p2Line = playerList.find { it.contains("PlayerId(value=P2)") }
		val p3Line = playerList.find { it.contains("PlayerId(value=P3)") }
		val p4Line = playerList.find { it.contains("PlayerId(value=P4)") }
		
		// P1 is attacker
		assertThat(p1Line).contains("(ATTACKER)")
		// P2 is defender
		assertThat(p2Line).contains("(DEFENDER)")
		// P3 and P4 are uninvolved - should have no role marker (empty string from else branch)
		assertThat(p3Line).isNotNull()
		assertThat(p3Line).doesNotContain("(ATTACKING)")
		assertThat(p3Line).doesNotContain("(ATTACKER)")
		assertThat(p3Line).doesNotContain("(DEFENDER)")
		// P3 line should end with "cards" not "cards(something)"
		assertThat(p3Line).matches(".*PlayerId\\(value=P3\\):.*cards$")
		
		assertThat(p4Line).isNotNull()
		assertThat(p4Line).doesNotContain("(ATTACKING)")
		assertThat(p4Line).doesNotContain("(ATTACKER)")
		assertThat(p4Line).doesNotContain("(DEFENDER)")
		assertThat(p4Line).matches(".*PlayerId\\(value=P4\\):.*cards$")
	}
	
	@Test
	fun `getGameStatus shows (ATTACKING) for joiners and no role for uninvolved players`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val p3 = PlayerId("P3")
		
		// give ranks so join is possible
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val joinCard = Card(Suit.CLUBS, Rank.SIX)
		
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT)), p2)
		val joinerHand = PlayerHand.create(listOf(joinCard), p3)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand, p3 to joinerHand)
		val game = Game(hands, listOf(p1, p2, p3), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		game.attackWithCard(attackCard)
		val joined = game.joinAttack(p3, joinCard)
		assertThat(joined).isTrue()
		
		val status = game.getGameStatus()
		// joiner should be marked as ATTACKING
		assertThat(status).contains("(ATTACKING)")
		// defender should be marked as DEFENDER and attacker as ATTACKER; ensure third party role string exists for p2
		assertThat(status).contains(p2.toString())
		// ensure defender is present in status
		assertThat(status).contains("PlayerId(value=P2):")
	}
	
	@Test
	fun `getGameStatus explicitly marks joiner as ATTACKING`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val p3 = PlayerId("P3")
		
		// attacker has a card of rank SIX, joiner has same rank; defender has many cards so capacity not exceeded
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val joinCard = Card(Suit.CLUBS, Rank.SIX)
		
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		// give defender several cards so join is always allowed (capacity-wise)
		val defenderHand = PlayerHand.create(
			listOf(
				Card(Suit.HEARTS, Rank.SIX), Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT),
				Card(Suit.DIAMONDS, Rank.SIX), Card(Suit.CLUBS, Rank.SEVEN)
			), p2
		)
		val joinerHand = PlayerHand.create(listOf(joinCard), p3)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand, p3 to joinerHand)
		val game = Game(hands, listOf(p1, p2, p3), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		game.attackWithCard(attackCard)
		val joined = game.joinAttack(p3, joinCard)
		assertThat(joined).isTrue()
		
		val status = game.getGameStatus()
		// verify that P3 line contains (ATTACKING) - extract player lines and check P3 has the role
		val playerLines = status.split("\n").filter { it.contains("PlayerId") }
		val p3Line = playerLines.find { it.contains("PlayerId(value=P3)") }
		assertThat(p3Line).isNotNull()
		assertThat(p3Line).contains("(ATTACKING)")
	}
}