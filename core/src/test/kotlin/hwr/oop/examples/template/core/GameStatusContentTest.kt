package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GameStatusContentTest {

    @Test
    fun `isRoundActive reflects constructor flag`() {
        val p1 = PlayerId("P1")
        val p2 = PlayerId("P2")

        val hands = mapOf(
            p1 to PlayerHand.create(emptyList(), p1),
            p2 to PlayerHand.create(emptyList(), p2)
        )

        val game = Game(hands, listOf(p1, p2), MutableDeck(mutableListOf()), roundActive = false)
        assertThat(game.isRoundActive()).isFalse()

        // flip to active via reflection to ensure the other state is reachable
        val field = Game::class.java.getDeclaredField("roundActive")
        field.isAccessible = true
        field.setBoolean(game, true)
        assertThat(game.isRoundActive()).isTrue()
    }

    @Test
    fun `getGameStatus shows defended and undefended counts and roles`() {
        val attacker = PlayerId("A")
        val defender = PlayerId("D")

        val attackCard1 = Card(Suit.SPADES, Rank.SIX)
        val attackCard2 = Card(Suit.CLUBS, Rank.SIX)
        val defendCard = Card(Suit.HEARTS, Rank.KING)

        val attackerHand = PlayerHand.create(listOf(attackCard1, attackCard2), attacker)
        val defenderHand = PlayerHand.create(listOf(defendCard), defender)

        val hands = mapOf(attacker to attackerHand, defender to defenderHand)
        val deckCards = mutableListOf<Card>()
        deckCards.add(Card(Suit.HEARTS, Rank.ACE))
        val deck = MutableDeck(deckCards)

        val game = Game(hands, listOf(attacker, defender), deck, roundActive = true)

        // attack with two cards; defend only the first
        game.attackWithCard(attackCard1)
        game.attackWithCard(attackCard2)
        game.defendCard(attackCard1, defendCard)

        val status = game.getGameStatus()
        assertThat(status).contains("(Defended: 1, Undefended: 1)")
        assertThat(status).contains("Current Attacker: $attacker")
        assertThat(status).contains("Current Defender: $defender")
    }

    @Test
    fun `replenishHands does nothing when players already have six cards`() {
        val p1 = PlayerId("P1")
        val p2 = PlayerId("P2")

        // give both players exactly 6 cards
        val cards = mutableListOf<Card>()
        for (i in 1..6) cards.add(Card(Suit.HEARTS, Rank.values()[(i - 1) % Rank.values().size]))

        val attackerHand = PlayerHand.create(cards.toList(), p1)
        val defenderHand = PlayerHand.create(cards.toList(), p2)

        val deck = MutableDeck(mutableListOf(Card(Suit.SPADES, Rank.ACE)))
        val hands = mapOf(p1 to attackerHand, p2 to defenderHand)

        val game = Game(hands, listOf(p1, p2), deck, roundActive = true)
        game.replenishHands()

        assertThat(game.getPlayerHand(p1)?.cards()?.size).isEqualTo(6)
        assertThat(game.getPlayerHand(p2)?.cards()?.size).isEqualTo(6)
    }
}
