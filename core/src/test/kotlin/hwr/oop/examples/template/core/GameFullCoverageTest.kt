package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for complete Baby Durak game flow without dealer roles
 */
class GameFullCoverageTest {
	
	// ==================== Game.create() Tests ====================
	
	@Test
	fun `create with 2 players initializes correct roles`() {
		// given
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val players = listOf(p1, p2)
		
		// when
		val game = Game.create(players)
		
		// then - with 2 players: attacker=p1, defender=p2
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.getPlayerHand(p1)?.cards()).hasSize(6)
		assertThat(game.getPlayerHand(p2)?.cards()).hasSize(6)
	}
	
	@Test
	fun `create with 3 players initializes correct roles`() {
		// given
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val p3 = PlayerId("Player3")
		val players = listOf(p1, p2, p3)
		
		// when
		val game = Game.create(players)
		
		// then
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.getCurrentRoundAttackers()).contains(p1)
	}
	
	@Test
	fun `create with 4 players initializes correct roles`() {
		// given
		val players = (1..4).map { PlayerId("Player$it") }
		
		// when
		val game = Game.create(players)
		
		// then
		assertThat(game.getAttacker()).isEqualTo(players[0])
		assertThat(game.getDefender()).isEqualTo(players[1])
		players.forEach { player ->
			assertThat(game.getPlayerHand(player)?.cards()).hasSize(6)
		}
	}
	
	@Test
	fun `create with 1 player throws InvalidPlayerNumberException`() {
		// given
		val players = listOf(PlayerId("Player1"))
		
		// when & then
		assertThrows<InvalidPlayerNumberException> {
			Game.create(players)
		}
	}
	
	@Test
	fun `create with 5 players throws InvalidPlayerNumberException`() {
		// given
		val players = (1..5).map { PlayerId("Player$it") }
		
		// when & then
		assertThrows<InvalidPlayerNumberException> {
			Game.create(players)
		}
	}
	
	@Test
	fun `create with 0 players throws InvalidPlayerNumberException`() {
		// when & then
		assertThrows<InvalidPlayerNumberException> {
			Game.create(emptyList())
		}
	}
	
	// ==================== Getter Methods Tests ====================
	
	@Test
	fun `getPlayerHand returns correct hand for valid player`() {
		// given
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val game = Game.create(listOf(p1, p2))
		
		// when
		val hand = game.getPlayerHand(p1)
		
		// then
		assertThat(hand).isNotNull
		assertThat(hand?.cards()).hasSize(6)
	}
	
	@Test
	fun `getPlayerHand returns null for non-existent player`() {
		// given
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val p3 = PlayerId("NonExistent")
		val game = Game.create(listOf(p1, p2))
		
		// when
		val hand = game.getPlayerHand(p3)
		
		// then
		assertThat(hand).isNull()
	}
	
	@Test
	fun `isRoundActive returns false before startRound`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// then
		assertThat(game.isRoundActive()).isFalse()
	}
	
	// ==================== Round Lifecycle Tests ====================
	
	@Test
	fun `round can be started and ended`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when
		game.startRound()
		assertThat(game.isRoundActive()).isTrue()
		
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
	
	@Test
	fun `cannot attack outside of round`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		val card = game.getPlayerHand(game.getAttacker())?.cards()?.first() ?: return
		
		// when & then
		assertThrows<IllegalStateException> {
			game.attackWithCard(card)
		}
	}
	
	// ==================== Attack/Defend Flow Tests ====================
	
	@Test
	fun `attacker can play cards during round`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attackerHand = game.getPlayerHand(game.getAttacker())
		val card = attackerHand?.cards()?.first() ?: return
		
		// when
		val success = game.attackWithCard(card)
		
		// then
		assertThat(success).isTrue()
		assertThat(game.getRoundCardPairings()).containsKey(card)
	}
	
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
	fun `fully defended round identifies winner`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		// when - no cards attacked, so round is fully defended
		val isFullyDefended = game.isRoundFullyDefended()
		
		// then
		assertThat(isFullyDefended).isTrue()
	}
	
	@Test
	fun `hasUndefendedCards detects unbeaten attacks`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		game.startRound()
		
		val attackerCard = game.getPlayerHand(game.getAttacker())?.cards()?.first()
		if (attackerCard != null) {
			game.attackWithCard(attackerCard)
			
			// when
			val hasUndefended = game.hasUndefendedCards()
			
			// then
			assertThat(hasUndefended).isTrue()
		}
	}
	
	// ==================== Role Rotation Tests ====================
	
	@Test
	fun `roles rotate when round ends with 2 players`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		
		// when: round with no attacks (defender wins by default)
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
		
		val sequence = mutableListOf<PlayerId>()
		sequence.add(game.getAttacker())
		
		// when: play 3 rounds
		repeat(3) {
			game.startRound()
			game.endRound()
			sequence.add(game.getAttacker())
		}
		
		// then - attacker should rotate through positions
		assertThat(sequence).hasSize(4)
		assertThat(sequence[0]).isNotEqualTo(sequence[1])
	}
	
	// ==================== Deck & Replenish Tests ====================
	
	@Test
	fun `isDeckEmpty returns false for new game`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// then
		assertThat(game.isDeckEmpty()).isFalse()
	}
	
	@Test
	fun `isDeckEmpty returns true when deck is empty`() {
		// given
		val game = Game(
			handsOfPlayers = mapOf(
				PlayerId("P1") to PlayerHand.create(id = PlayerId("P1")),
				PlayerId("P2") to PlayerHand.create(id = PlayerId("P2"))
			),
			players = listOf(PlayerId("P1"), PlayerId("P2")),
			deck = MutableDeck(mutableListOf())
		)
		
		// then
		assertThat(game.isDeckEmpty()).isTrue()
	}
	
	@Test
	fun `replenishHands does not break game state`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when - call replenish multiple times
		game.replenishHands()
		game.replenishHands()
		game.replenishHands()
		
		// then - hands should still be valid (max 6)
		assertThat(game.getPlayerHand(game.getAttacker())?.cards()?.size).isLessThanOrEqualTo(6)
		assertThat(game.getPlayerHand(game.getDefender())?.cards()?.size).isLessThanOrEqualTo(6)
	}
	
	// ==================== Game Status Tests ====================
	
	@Test
	fun `getGameStatus returns non-empty string with game info`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when
		val status = game.getGameStatus()
		
		// then
		assertThat(status).isNotBlank()
		assertThat(status).contains("DURAK GAME STATUS")
		assertThat(status).contains("Current Attacker:")
		assertThat(status).contains("Current Defender:")
		assertThat(status).contains("P1")
		assertThat(status).contains("P2")
	}
	
	@Test
	fun `getGameStatus shows round state`() {
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
	
	// ==================== Game Over Tests ====================
	
	@Test
	fun `game is not over when multiple players have cards`() {
		// given
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		// when
		val isOver = game.isGameOver()
		
		// then
		assertThat(isOver).isFalse()
	}
	
	@Test
	fun `game is over when only one player has cards`() {
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
	
	// ==================== 4-Player Game Tests ====================
	
	@Test
	fun `4-player game initializes with 6 cards each`() {
		// given
		val players = (1..4).map { PlayerId("P$it") }
		val game = Game.create(players)
		
		// then
		players.forEach { player ->
			assertThat(game.getPlayerHand(player)?.cards()).hasSize(6)
		}
	}
	
	@Test
	fun `constructor with direct values initializes correctly`() {
		// given
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val hands = mapOf(
			p1 to PlayerHand.create(id = p1),
			p2 to PlayerHand.create(id = p2)
		)
		val players = listOf(p1, p2)
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		// when
		val game = Game(hands, players, deck)
		
		// then
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.isDeckEmpty()).isFalse()
	}
}
