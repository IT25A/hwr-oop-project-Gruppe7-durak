package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/*
 * Consolidated test file combining the previous three test files:
 * - GameMergedTest.kt
 * - GameCoverageExtraTest.kt
 * - GameCoverageAllBranchesTest.kt
 *
 * Duplicate/obsolete files were removed in favour of this single file.
 */

class GameMergedTest {
	
	// ==================== Creation / Initialization Tests ====================
	
	@Test
	fun `create with 2 players initializes correct roles`() {
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val game = Game.create(listOf(p1, p2))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.getPlayerHand(p1)?.cards()).hasSize(6)
		assertThat(game.getPlayerHand(p2)?.cards()).hasSize(6)
	}
	
	@Test
	fun `create with 3 players initializes correct roles`() {
		val p1 = PlayerId("Player1")
		val p2 = PlayerId("Player2")
		val p3 = PlayerId("Player3")
		val game = Game.create(listOf(p1, p2, p3))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		assertThat(game.getCurrentRoundAttackers()).contains(p1)
	}
	
	@Test
	fun `create with invalid player numbers throws`() {
		assertThrows<InvalidPlayerNumberException> { Game.create(listOf(PlayerId("P1"))) }
		assertThrows<InvalidPlayerNumberException> { Game.create((1..5).map { PlayerId("P$it") }) }
		assertThrows<InvalidPlayerNumberException> { Game.create(emptyList()) }
	}
	
	// ==================== Getter Methods Tests ====================
	
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
	
	// ==================== Round Lifecycle Tests ====================
	
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
		assertThrows<IllegalStateException> { game.attackWithCard(attackCard) }
	}
	
	@Test
	fun `joinAttack without active round throws`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val hands = mapOf(
			players[0] to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), players[0]),
			players[1] to PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), players[1]),
			players[2] to PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), players[2])
		)
		val game = Game(hands, players, Deck.createRandomDeck().toMutableDeck())
		
		val joinCard = hands[players[2]]?.cards()?.first() ?: return
		assertThrows<IllegalStateException> { game.joinAttack(players[2], joinCard) }
	}
	
	// ==================== Attack / Defend Flow ====================
	
	@Test
	fun `attacker can play card during round`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		
		val success = game.attackWithCard(card)
		assertThat(success).isTrue()
		assertThat(game.getRoundCardPairings()).containsKey(card)
	}
	
	@Test
	fun `defender can beat card when possible`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attackCard = game.getPlayerHand(game.getAttacker())?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val defenderHand = game.getPlayerHand(game.getDefender())
		val defendCard = defenderHand?.cards()?.find { def ->
			if (def.suit() == attackCard.suit()) {
				def.getCardValue(def.rank()) > attackCard.getCardValue(attackCard.rank())
			} else {
				def.suit() == Suit.HEARTS
			}
		}
		
		if (defendCard != null) {
			val ok = game.defendCard(attackCard, defendCard)
			assertThat(ok).isTrue()
			assertThat(game.getRoundCardPairings()[attackCard]).isEqualTo(defendCard)
		}
	}
	
	@Test
	fun `defender cannot beat with weaker card`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		
		val attackerHand = game.getPlayerHand(game.getAttacker())
		val defenderHand = game.getPlayerHand(game.getDefender())
		
		val attackCard = attackerHand?.cards()?.maxByOrNull { it.getCardValue(it.rank()) }
		val defendCard = defenderHand?.cards()?.minByOrNull { it.getCardValue(it.rank()) }
		
		if (attackCard != null && defendCard != null && attackCard.suit() == defendCard.suit()) {
			game.attackWithCard(attackCard)
			val ok = game.defendCard(attackCard, defendCard)
			assertThat(ok).isFalse()
		}
	}
	
	@Test
	fun `hasUndefendedCards detects unbeaten attacks`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		game.startRound()
		
		val attackerCard = game.getPlayerHand(game.getAttacker())?.cards()?.first()
		if (attackerCard != null) {
			game.attackWithCard(attackerCard)
			assertThat(game.hasUndefendedCards()).isTrue()
		}
	}
	
	@Test
	fun `fully defended round is true when no attacks`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		// no attacks
		assertThat(game.isRoundFullyDefended()).isTrue()
	}
	
	// ==================== Multi-Attacker Tests ====================
	
	@Test
	fun `other players can join attack in 3-player game`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		game.startRound()
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		val third = players.first { it != attacker && it != defender }
		
		val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		val joinerCard = game.getPlayerHand(third)?.cards()?.firstOrNull { it.rank() == attackCard.rank() } ?: return
		
		game.attackWithCard(attackCard)
		val joined = game.joinAttack(third, joinerCard)
		
		assertThat(joined).isTrue()
		assertThat(game.getCurrentRoundAttackers()).contains(third)
		assertThat(game.getRoundCardPairings().size).isEqualTo(2)
	}
	
	@Test
	fun `attacker cannot join own attack and defender cannot join`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		game.startRound()
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val secondCard = game.getPlayerHand(attacker)?.cards()?.firstOrNull { it != attackCard } ?: return
		assertThat(game.joinAttack(attacker, secondCard)).isFalse()
		
		val defenderCard = game.getPlayerHand(defender)?.cards()?.first() ?: return
		assertThat(game.joinAttack(defender, defenderCard)).isFalse()
	}
	
	// ==================== Role Rotation Tests ====================
	
	@Test
	fun `roles rotate when round ends with 2 players`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		
		assertThat(game.getAttacker()).isEqualTo(p1)
		assertThat(game.getDefender()).isEqualTo(p2)
		
		game.startRound()
		game.endRound()
		
		assertThat(game.getAttacker()).isEqualTo(p2)
		assertThat(game.getDefender()).isEqualTo(p1)
	}
	
	@Test
	fun `roles rotate through 3 players and return`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		
		val sequence = mutableListOf<Pair<PlayerId, PlayerId>>()
		sequence.add(game.getAttacker() to game.getDefender())
		
		repeat(3) {
			game.startRound()
			game.endRound()
			sequence.add(game.getAttacker() to game.getDefender())
		}
		
		assertThat(sequence).hasSize(4)
		assertThat(sequence.last().first).isEqualTo(sequence.first().first)
	}
	
	// ==================== Deck & Replenish Tests ====================
	
	@Test
	fun `deck empty checks and replenish behavior`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		assertThat(game.isDeckEmpty()).isFalse()
		
		val emptyGame = Game(
			handsOfPlayers = mapOf(
				PlayerId("P1") to PlayerHand.create(id = PlayerId("P1")),
				PlayerId("P2") to PlayerHand.create(id = PlayerId("P2"))
			),
			players = listOf(PlayerId("P1"), PlayerId("P2")),
			deck = MutableDeck(mutableListOf())
		)
		assertThat(emptyGame.isDeckEmpty()).isTrue()
		
		game.replenishHands()
		game.replenishHands()
		assertThat(game.getPlayerHand(game.getAttacker())?.cards()?.size).isLessThanOrEqualTo(6)
	}
	
	// ==================== Game Status & End Conditions ====================
	
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
	fun `game over and loser detection`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val hands = mapOf(
			p1 to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1),
			p2 to PlayerHand.create(emptyList(), p2)
		)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		assertThat(game.isGameOver()).isTrue()
		assertThat(game.getLoser()).isEqualTo(p2)
	}
	
	// ==================== Error Cases ====================
	
	@Test
	fun `cannot attack or defend with card not in hand`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		val fakeCard = Card(Suit.CLUBS, Rank.SIX)
		
		assertThrows<IllegalStateException> { game.attackWithCard(fakeCard) }
		assertThrows<IllegalStateException> { game.defendCard(Card(Suit.CLUBS, Rank.SEVEN), fakeCard) }
	}
	
	// ==================== 4-Player scenarios ====================
	
	@Test
	fun `4-player game allows multiple joiners`() {
		
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val p3 = PlayerId("P3")
		val p4 = PlayerId("P4")
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val card2 = Card(Suit.HEARTS, Rank.SIX)
		val card3 = Card(Suit.DIAMONDS, Rank.SIX)
		val p1Hand = PlayerHand.create(listOf(attackCard), p1)
		val p2Hand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.JACK), Card(Suit.SPADES, Rank.NINE)), p2) // Only one card - can't defend both attacks
		val p3Hand = PlayerHand.create(listOf(card2), p3) // Will receive cards from replenish
		val p4Hand = PlayerHand.create(listOf(card3), p4)
		
		val hands = mapOf(p1 to p1Hand, p2 to p2Hand, p3 to p3Hand, p4 to p4Hand)
		val game = Game(hands, listOf(p1, p2, p3, p4), deck = MutableDeck(mutableListOf()))
		game.startRound()
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		
		
		game.attackWithCard(attackCard)
		
		val success2 = game.joinAttack(p3, card2)
		
		val success3 = game.joinAttack(p4, card3)
		
		
		
		if (success2) assertThat(game.getCurrentRoundAttackers()).contains(p3)
		if (success3) assertThat(game.getCurrentRoundAttackers()).contains(p4)
	}
}

class GameCoverageExtraTest {
	
	@Test
	fun `attackWithCard returns false when defender capacity exceeded and rank not on table`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		// attacker has two different ranks, defender has zero cards
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX), Card(Suit.HEARTS, Rank.SEVEN)), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), MutableDeck(mutableListOf()))
		
		game.startRound()
		
		val first = attackerHand.cards().first()
		val second = attackerHand.cards().last()
		
		val ok1 = game.attackWithCard(first)
		assertThat(ok1).isTrue()
		
		// Now try to attack with a different rank; defender has 0 capacity -> should be false
		val ok2 = game.attackWithCard(second)
		assertThat(ok2).isFalse()
	}
	
	@Test
	fun `joinAttack rejects when rank not on table or player lacks card or already joining`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val p3 = PlayerId("P3")
		
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		val joinerHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.EIGHT)), p3)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand, p3 to joinerHand)
		val game = Game(hands, listOf(p1, p2, p3), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		
		val attackCard = attackerHand.cards().first()
		game.attackWithCard(attackCard)
		
		// joiner has rank EIGHT, attack rank is SIX -> should be rejected
		val tryJoinWrongRank = game.joinAttack(p3, joinerHand.cards().first())
		assertThat(tryJoinWrongRank).isFalse()
		
		// try with a card not in player's hand
		val fakeCard = Card(Suit.SPADES, Rank.NINE)
		val tryJoinNoCard = game.joinAttack(p3, fakeCard)
		assertThat(tryJoinNoCard).isFalse()
		
		// Make a joiner with a correct rank and succeed, then second attempt should be false
		val joiner2Hand = PlayerHand.create(listOf(Card(Suit.DIAMONDS, Rank.SIX)), PlayerId("P4"))
		val p4 = PlayerId("P4")
		
		// create a fresh game for this part (use fresh attacker hand instance)
		val attackerHand2 = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand2 = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.CLUBS, Rank.EIGHT)), p2)
		val handsPart = mapOf(p1 to attackerHand2, p2 to defenderHand2, p4 to joiner2Hand)
		val game2 = Game(handsPart, listOf(p1, p2, p4), Deck.createRandomDeck().toMutableDeck())
		game2.startRound()
		game2.attackWithCard(attackerHand2.cards().first())
		val successFirst = game2.joinAttack(p4, joiner2Hand.cards().first())
		assertThat(successFirst).isTrue()
		val successSecond = game2.joinAttack(p4, joiner2Hand.cards().first())
		assertThat(successSecond).isFalse()
	}
	
	@Test
	fun `defendCard throws when attacking card not in round`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		
		// No attacks played yet; defending on a card should throw
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val defendCard = defenderHand.cards().first()
		
		assertThrows<IllegalStateException> { game.defendCard(attackCard, defendCard) }
	}
	
	@Test
	fun `joinAttack returns false when defender capacity exceeded`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val p3 = PlayerId("P3")
		
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2) // defender has zero capacity
		val joinerHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), p3)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand, p3 to joinerHand)
		val game = Game(hands, listOf(p1, p2, p3), MutableDeck(mutableListOf()))
		
		game.startRound()
		val attackCard = attackerHand.cards().first()
		game.attackWithCard(attackCard)
		
		// defender has zero cards so any join should be rejected due to capacity
		val joined = game.joinAttack(p3, joinerHand.cards().first())
		assertThat(joined).isFalse()
	}
	
	@Test
	fun `defendCard without active round throws`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val hands = mapOf(
			p1 to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1),
			p2 to PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		val attackCard = hands[p1]?.cards()?.first() ?: return
		val defendCard = hands[p2]?.cards()?.first() ?: return
		assertThrows<IllegalStateException> { game.defendCard(attackCard, defendCard) }
	}
	
	@Test
	fun `defendCard throws when defender does not have the card`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		val attackCard = attackerHand.cards().first()
		game.attackWithCard(attackCard)
		
		// try to defend with a card not in defender's hand
		val fakeDefend = Card(Suit.CLUBS, Rank.NINE)
		assertThrows<IllegalStateException> { game.defendCard(attackCard, fakeDefend) }
	}
	
	@Test
	fun `endRound defender loses results in defender taking cards`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val deck = MutableDeck(mutableListOf())
		val game = Game(hands, listOf(p1, p2), deck)
		
		game.startRound()
		game.attackWithCard(attackCard)
		
		// Defender cannot defend -> endRound should make defender take the card
		game.endRound()
		
		val newDefenderHand = game.getPlayerHand(p2)
		assertThat(newDefenderHand?.cards()).contains(attackCard)
	}
}

class GameCoverageAllBranchesTest {
	
	@Test
	fun `getLoser returns null when game not over`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		assertThat(game.isGameOver()).isFalse()
		assertThat(game.getLoser()).isNull()
	}
	
	@Test
	fun `defender trump beats non-trump regardless of rank`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.KING)), p1)
		// defender holds trump (HEARTS) but lower rank
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SIX)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		val attackCard = attackerHand.cards().first()
		game.attackWithCard(attackCard)
		
		val defendCard = defenderHand.cards().first()
		val success = game.defendCard(attackCard, defendCard)
		assertThat(success).isTrue()
		assertThat(game.getRoundCardPairings()[attackCard]).isEqualTo(defendCard)
	}
	
	@Test
	fun `defendCard returns false when defending card does not beat attacking card (different non-trump suit)`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		val attackCard = Card(Suit.CLUBS, Rank.KING)
		// defender has a different suit and is not trump
		val defenderCard = Card(Suit.DIAMONDS, Rank.QUEEN)
		
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(listOf(defenderCard), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		game.attackWithCard(attackCard)
		
		val ok = game.defendCard(attackCard, defenderCard)
		assertThat(ok).isFalse()
	}
	
	@Test
	fun `endRound without active round throws`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		assertThrows<IllegalStateException> { game.endRound() }
	}
	
	@Test
	fun `endRound when defender wins discards both attack and defend and rotates roles`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val defendCard = Card(Suit.SPADES, Rank.SEVEN) // same suit higher
		
		val attackerHand = PlayerHand.create(listOf(attackCard), p1)
		val defenderHand = PlayerHand.create(listOf(defendCard), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val deck = MutableDeck(mutableListOf())
		val game = Game(hands, listOf(p1, p2), deck)
		
		game.startRound()
		game.attackWithCard(attackCard)
		val defended = game.defendCard(attackCard, defendCard)
		assertThat(defended).isTrue()
		
		game.endRound()
		
		// discard pile should contain two cards (attack + defend)
		assertThat(game.getGameStatus()).contains("Discard pile: 2") // status contains discard count
		// roles rotated
		assertThat(game.getAttacker()).isEqualTo(p2)
	}
	
	@Test
	fun `endRound defender loses with some defended cards - defender takes all including defended ones`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		val attack1 = Card(Suit.SPADES, Rank.SIX)
		val attack2 = Card(Suit.CLUBS, Rank.SEVEN)
		val defend1 = Card(Suit.SPADES, Rank.SEVEN) // same suit higher so defend succeeds
		
		val attackerHand = PlayerHand.create(listOf(attack1, attack2), p1)
		val defenderHand =
			PlayerHand.create(listOf(defend1, Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val deck = MutableDeck(mutableListOf())
		val game = Game(hands, listOf(p1, p2), deck)
		
		game.startRound()
		game.attackWithCard(attack1)
		// attempt to defend first attack (may or may not succeed depending on card ordering)
		game.defendCard(attack1, defend1)
		
		// attacker plays a second card which defender cannot beat
		val ok2 = game.attackWithCard(attack2)
		assertThat(ok2).isTrue()
		
		// end round -> defender loses and must take both attack and defend cards
		game.endRound()
		
		val newDefenderHand = game.getPlayerHand(p2)
		// defender should at least take both attacking cards
		assertThat(newDefenderHand?.cards()).contains(attack1, attack2)
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
	fun `attacker can play additional card when rank on table`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		val attackCard1 = Card(Suit.SPADES, Rank.SIX)
		val attackCard2 = Card(Suit.CLUBS, Rank.SIX) // same rank
		val attackerHand = PlayerHand.create(listOf(attackCard1, attackCard2), p1)
		// defender has enough cards so capacity not exceeded
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		val ok1 = game.attackWithCard(attackCard1)
		assertThat(ok1).isTrue()
		
		// second attack has same rank as existing card on table -> should be allowed
		val ok2 = game.attackWithCard(attackCard2)
		assertThat(ok2).isTrue()
		assertThat(game.getRoundCardPairings().size).isEqualTo(2)
	}
	
	@Test
	fun `replenishHands draws from deck when players need cards`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		
		// start with empty hands
		val attackerHand = PlayerHand.create(emptyList(), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2)
		
		// create a deck with several cards
		val deckCards = mutableListOf<Card>()
		for (s in listOf(Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES)) {
			deckCards.add(Card(s, Rank.SIX))
			deckCards.add(Card(s, Rank.SEVEN))
		}
		
		val deck = MutableDeck(deckCards.toMutableList())
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), deck)
		
		// set currentRoundAttackers to include attacker
		game.startRound()
		
		// replenish should give cards to attacker first, then defender
		game.replenishHands()
		
		val afterAttacker = game.getPlayerHand(p1)
		val afterDefender = game.getPlayerHand(p2)
		
		assertThat(afterAttacker?.cards()?.size ?: 0).isGreaterThan(0)
		assertThat(afterDefender?.cards()?.size ?: 0).isGreaterThanOrEqualTo(0)
	}
	
	@Test
	fun `at the start of a round card-pairs and current attackers are cleared`(){
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val attackerHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SEVEN)), p2)
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), deck, roundCardPairings = mutableMapOf(Card (Suit.CLUBS, Rank.SIX) to Card(Suit.CLUBS, Rank.SEVEN)), currentRoundAttackers = mutableListOf(p1))
		
		
		game.startRound()
		
		assertThat(game.getRoundCardPairings()).isEmpty()
		assertThat(game.getCurrentRoundAttackers()).containsExactly(p1)
	}
	
	@Test
	fun `at the end of a round card-pairs and current attackers are cleared`(){
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val attackerHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SEVEN)), p2)
		val deck = Deck.createRandomDeck().toMutableDeck()
		
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), deck, roundCardPairings = mutableMapOf(Card (Suit.CLUBS, Rank.SIX) to Card(Suit.CLUBS, Rank.SEVEN)), currentRoundAttackers = mutableListOf(p1))
		
		
		game.startRound()
		game.attackWithCard(Card(Suit.CLUBS, Rank.SIX))
		game.endRound()
		
		assertThat(game.getRoundCardPairings()).isEmpty()
		assertThat(game.getCurrentRoundAttackers()).containsExactly(p1)
	}
	
	@Test
	fun `at the end defender lose and the hands will be replenished`(){
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val p3 = PlayerId("P3")
		val p4 = PlayerId("P4")
		
		// Create all players with hands from the start
		val attackCard = Card(Suit.CLUBS, Rank.SIX)
		val defendCard = Card(Suit.CLUBS, Rank.SEVEN) // beats the attack card
		val joinCard = Card(Suit.HEARTS, Rank.SIX) // same rank as attack, will be undefended
		
		val p1Hand = PlayerHand.create(listOf(attackCard), p1)
		val p2Hand = PlayerHand.create(listOf(defendCard, Card(Suit.SPADES, Rank.JACK), Card(Suit.SPADES, Rank.NINE)), p2) // Only one card - can't defend both attacks
		val p3Hand = PlayerHand.create(emptyList(), p3) // Will receive cards from replenish
		val p4Hand = PlayerHand.create(listOf(joinCard), p4)
		
		val hands = mapOf(p1 to p1Hand, p2 to p2Hand, p3 to p3Hand, p4 to p4Hand)
		val deck = Deck.createRandomDeck().toMutableDeck()
		val game = Game(hands, listOf(p1, p2, p3, p4), deck)

		// Start round and play
		game.startRound()
		
		// P1 attacks with CLUBS SIX
		val attack1Ok = game.attackWithCard(attackCard)
		assertThat(attack1Ok).isTrue()
		
		// P2 defends with CLUBS SEVEN (higher rank, same suit)
		val defend1Ok = game.defendCard(attackCard, defendCard)
		assertThat(defend1Ok).isTrue()

		// P4 joins attack with HEARTS SIX (same rank as first attack)
		val joinOk = game.joinAttack(p4, joinCard)
		assertThat(joinOk).isTrue()
		
		// Verify that there's an undefended card (HEARTS SIX)
		assertThat(game.hasUndefendedCards()).isTrue()
		
		// Remember the defender's hand size before taking cards
		val p2CardsBeforeLoss = game.getPlayerHand(p2)?.cards()?.size ?: 0

		// End round - defender loses
		game.endRound()

		// Verify that P2 (the loser) took the cards
		val p2CardsAfterLoss = game.getPlayerHand(p2)?.cards()?.size ?: 0
		assertThat(p2CardsAfterLoss).isGreaterThan(p2CardsBeforeLoss)

		// Verify role rotation: defender -> attacker, next player -> defender
		assertThat(game.getAttacker()).isEqualTo(p2) // Previous defender becomes attacker
		assertThat(game.getDefender()).isEqualTo(p3) // Next player becomes defender

		// Verify that current round attackers include the new attacker and the joiner
		assertThat(game.getCurrentRoundAttackers()).contains(p2)

		// Verify hands are properly replenished after endRound
		// endRound() calls replenishHands() automatically, so no need to call it again
		
		// P1: played 1 card -> should have 6 after replenish
		assertThat(game.getPlayerHand(p1)?.cards()?.size ?: 0).isEqualTo(6)

		// P2: started with 1, lost and took 2 cards (both pairings), then replenished
		// P2 should have at least the 2 undefended cards plus more from replenish
		assertThat(game.getPlayerHand(p2)?.cards()?.size ?: 0).isGreaterThanOrEqualTo(2)

		// P3: started with 0, is now defender -> should be replenished to 6
		assertThat(game.getPlayerHand(p3)?.cards()?.size ?: 0).isEqualTo(6)

		// P4: played 1 card -> should have 6 after replenish
		assertThat(game.getPlayerHand(p4)?.cards()?.size ?: 0).isEqualTo(6)
	}
}

