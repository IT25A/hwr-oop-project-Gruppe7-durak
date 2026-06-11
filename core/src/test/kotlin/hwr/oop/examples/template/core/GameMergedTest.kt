package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Consolidated tests from GameCompleteDurakTest and GameFullCoverageTest.
 * Duplicate tests were removed and remaining cases combined here.
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
        val players = (1..4).map { PlayerId("P$it") }
        val game = Game.create(players)
        game.startRound()

        val attacker = game.getAttacker()
        val defender = game.getDefender()
        val others = players.filter { it != attacker && it != defender }
        val joiner2 = others.getOrNull(0) ?: return
        val joiner3 = others.getOrNull(1) ?: return

        val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
        game.attackWithCard(attackCard)

        val card2 = game.getPlayerHand(joiner2)?.cards()?.firstOrNull { it.rank() == attackCard.rank() } ?: game.getPlayerHand(joiner2)!!.cards().first()
        val success2 = game.joinAttack(joiner2, card2)

        val card3 = game.getPlayerHand(joiner3)?.cards()?.firstOrNull { it.rank() == attackCard.rank() } ?: return
        val success3 = game.joinAttack(joiner3, card3)

        if (success2) assertThat(game.getCurrentRoundAttackers()).contains(joiner2)
        if (success3) assertThat(game.getCurrentRoundAttackers()).contains(joiner3)
    }
}

