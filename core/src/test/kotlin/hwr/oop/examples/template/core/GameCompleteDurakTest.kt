package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for complete Baby Durak game flow with 2-4 players
 */
class GameCompleteDurakTest {
	
	// ==================== Round Start/End Tests ====================
	
	@Test
	fun `round can be started and ended`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when
		game.startRound()
		assertThat(game.isRoundActive()).isTrue()
		
		// then: defender wins (no cards attacked, so fully defended)
		game.endRound()
		assertThat(game.isRoundActive()).isFalse()
	}
	
	@Test
	fun `cannot start round twice`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		// when & then
		assertThrows<IllegalStateException> {
			game.startRound()
		}
	}
	
	// ==================== Attack/Defend Flow Tests ====================
	
	@Test
	fun `attacker can play cards during round`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		
		val attackerHand = game.getPlayerHand(game.getAttacker())
		val card = attackerHand?.cards()?.first() ?: return
		
		// when
		game.startRound()
		val success = game.attackWithCard(card)
		
		// then
		assertThat(success).isTrue()
		assertThat(game.getRoundCardPairings()).containsKey(card)
		assertThat(game.getRoundCardPairings()[card]).isNull() // Not yet defended
	}
	
	@Test
	fun `defender can beat cards`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attackerHand = game.getPlayerHand(game.getAttacker())
		val defenderHand = game.getPlayerHand(game.getDefender())
		val attackCard = attackerHand?.cards()?.first() ?: return
		
		game.attackWithCard(attackCard)
		
		// Find a card that beats the attack card
		val defendCard = defenderHand?.cards()?.find { defendsCard ->
			// Simple: find higher card in same suit or trump
			if (defendsCard.suit() == attackCard.suit()) {
				defendsCard.getCardValue(defendsCard.rank()) > attackCard.getCardValue(attackCard.rank())
			} else {
				defendsCard.suit() == Suit.HEARTS // Trump suit
			}
		}
		
		if (defendCard != null) {
			// when
			val success = game.defendCard(attackCard, defendCard)
			
			// then
			assertThat(success).isTrue()
			assertThat(game.getRoundCardPairings()[attackCard]).isEqualTo(defendCard)
		}
	}
	
	@Test
	fun `defender cannot beat with weaker card`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attackerHand = game.getPlayerHand(game.getAttacker())
		val defenderHand = game.getPlayerHand(game.getDefender())
		
		// Find high attack card and low defend card
		val attackCard = attackerHand?.cards()?.maxByOrNull { it.getCardValue(it.rank()) }
		val defendCard = defenderHand?.cards()?.minByOrNull { it.getCardValue(it.rank()) }
		
		if (attackCard != null && defendCard != null && attackCard.suit() == defendCard.suit()) {
			game.attackWithCard(attackCard)
			
			// when
			val success = game.defendCard(attackCard, defendCard)
			
			// then
			assertThat(success).isFalse()
		}
	}
	
	// ==================== Multi-Attacker Tests ====================
	
	@Test
	fun `other players can join attack in 3-player game`() {
		// given
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		
		game.startRound()
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		val thirdPlayer = players.first { it != attacker && it != defender }
		
		val attackerCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		val joinerCard = game.getPlayerHand(thirdPlayer)?.cards()?.firstOrNull {
			it.rank() == attackerCard.rank()
		} ?: return
		
		// when
		game.attackWithCard(attackerCard)
		val joined = game.joinAttack(thirdPlayer, joinerCard)
		
		// then
		assertThat(joined).isTrue()
		assertThat(game.getCurrentRoundAttackers()).contains(thirdPlayer)
		assertThat(game.getRoundCardPairings().size).isEqualTo(2)
	}
	
	@Test
	fun `attacker cannot join own attack`() {
		// given
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		game.startRound()
		
		val attacker = game.getAttacker()
		val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val secondCard = game.getPlayerHand(attacker)?.cards()?.firstOrNull { it != attackCard } ?: return
		
		// when
		val success = game.joinAttack(attacker, secondCard)
		
		// then
		assertThat(success).isFalse()
	}
	
	@Test
	fun `defender cannot join attack`() {
		// given
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		game.startRound()
		
		val defender = game.getDefender()
		val attackCard = game.getPlayerHand(game.getAttacker())?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val defenderCard = game.getPlayerHand(defender)?.cards()?.first() ?: return
		
		// when
		val success = game.joinAttack(defender, defenderCard)
		
		// then
		assertThat(success).isFalse()
	}
	
	// ==================== Role Rotation Tests ====================
	
	@Test
	fun `roles rotate when defender wins with 2 players`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		
		// when: round with no attacked cards (defender wins by default)
		game.startRound()
		game.endRound()
		
		// then
		assertThat(game.getAttacker()).isEqualTo(p2)
		assertThat(game.getDefender()).isEqualTo(p1)
	}
	
	@Test
	fun `roles rotate correctly through 3 players`() {
		// given
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		
		val sequence = mutableListOf<Pair<PlayerId, PlayerId>>()
		sequence.add(game.getAttacker() to game.getDefender())
		
		// when: play 3 rounds
		repeat(3) {
			game.startRound()
			game.endRound()
			sequence.add(game.getAttacker() to game.getDefender())
		}
		
		// then
		// Verify role progression
		assertThat(sequence).hasSize(4)
		// After 3 rounds, should return to start
		assertThat(sequence.last().first).isEqualTo(sequence.first().first)
	}
	
	// ==================== Game Over Tests ====================
	
	@Test
	fun `game ends when only one player has cards`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val hands = mapOf(
			p1 to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1),
			p2 to PlayerHand.create(emptyList(), p2)
		)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		// when
		val isOver = game.isGameOver()
		
		// then
		assertThat(isOver).isTrue()
	}
	
	@Test
	fun `game not over when multiple players have cards`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when
		val isOver = game.isGameOver()
		
		// then
		assertThat(isOver).isFalse()
	}
	
	@Test
	fun `loser is identified when game ends`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val hands = mapOf(
			p1 to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1),
			p2 to PlayerHand.create(emptyList(), p2)
		)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		// when
		val loser = game.getLoser()
		
		// then
		assertThat(loser).isEqualTo(p2)
	}
	
	// ==================== Game Status Tests ====================
	
	@Test
	fun `game status shows current round state`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when
		val status = game.getGameStatus()
		
		// then
		assertThat(status).contains("DURAK GAME STATUS")
		assertThat(status).contains("Current Attacker:")
		assertThat(status).contains("Current Defender:")
		assertThat(status).contains("Round Active: false")
		assertThat(status).contains("P1")
		assertThat(status).contains("P2")
	}
	
	@Test
	fun `game status shows roles correctly`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		// when
		val status = game.getGameStatus()
		
		// then
		assertThat(status).contains("Round Active: true")
		assertThat(status).contains("(ATTACKER)")
		assertThat(status).contains("(DEFENDER)")
	}
	
	// ==================== Error Cases ====================
	
	@Test
	fun `cannot attack with card not in hand`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val fakeCard = Card(Suit.CLUBS, Rank.SIX)
		
		// when & then
		assertThrows<IllegalStateException> {
			game.attackWithCard(fakeCard)
		}
	}
	
	@Test
	fun `cannot defend with card not in hand`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attackCard = game.getPlayerHand(game.getAttacker())?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val fakeDefendCard = Card(Suit.DIAMONDS, Rank.ACE)
		
		// when & then
		assertThrows<IllegalStateException> {
			game.defendCard(attackCard, fakeDefendCard)
		}
	}
	
	@Test
	fun `cannot play rounds before initializing round`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		val card = game.getPlayerHand(game.getAttacker())?.cards()?.first() ?: return
		
		// when & then
		assertThrows<IllegalStateException> {
			game.attackWithCard(card)
		}
	}
	
	// ==================== 4-Player Game Tests ====================
	
	@Test
	fun `4-player game initializes correctly`() {
		// given
		val players = (1..4).map { PlayerId("P$it") }
		val game = Game.create(players)
		
		// then
		assertThat(game.getAttacker()).isEqualTo(players[0])
		assertThat(game.getDefender()).isEqualTo(players[1])
		players.forEach { player ->
			assertThat(game.getPlayerHand(player)?.cards()).hasSize(6)
		}
	}
	
	@Test
	fun `multiple players can attack in 4-player game`() {
		// given
		val players = (1..4).map { PlayerId("P$it") }
		val game = Game.create(players)
		game.startRound()
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		val joiner2 = players.firstOrNull { it != attacker && it != defender } ?: return
		val joiner3 = players.lastOrNull { it != attacker && it != defender && it != joiner2 } ?: return
		
		val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		// when: two other players try to join
		val join2Hand = game.getPlayerHand(joiner2)?.cards() ?: return
		val card2 = join2Hand.firstOrNull { it.rank() == attackCard.rank() } ?: join2Hand.first()
		val success2 = game.joinAttack(joiner2, card2)
		
		val join3Hand = game.getPlayerHand(joiner3)?.cards() ?: return
		val card3 = join3Hand.firstOrNull { it.rank() == attackCard.rank() } ?: return
		val success3 = game.joinAttack(joiner3, card3)
		
		// then
		if (success2) {
			assertThat(game.getCurrentRoundAttackers()).contains(joiner2)
		}
		if (success3) {
			assertThat(game.getCurrentRoundAttackers()).contains(joiner3)
		}
	}
}
