package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GameStatusVariantsTest {

    @Test
    fun `getGameStatus shows zero defended and undefended when no attacks`() {
        val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
        val status = game.getGameStatus()
        assertThat(status).contains("(Defended: 0, Undefended: 0)")
    }

    @Test
    fun `getGameStatus updates counts after defend`() {
        val p1 = PlayerId("P1")
        val p2 = PlayerId("P2")
        val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
        val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.KING)), p2)
        val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
        val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck(), roundActive = true)

        val attack = attackerHand.cards().first()
        val defend = defenderHand.cards().first()
        game.attackWithCard(attack)
        game.defendCard(attack, defend)

        val status = game.getGameStatus()
        assertThat(status).contains("(Defended: 1, Undefended: 0)")
        assertThat(game.hasUndefendedCards()).isFalse()
    }

    @Test
    fun `getGameStatus updates counts when attack not defended`() {
        val p1 = PlayerId("P1")
        val p2 = PlayerId("P2")
        val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
        val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
        val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
        val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck(), roundActive = true)

        val attack = attackerHand.cards().first()
        game.attackWithCard(attack)

        val status = game.getGameStatus()
        assertThat(status).contains("(Defended: 0, Undefended: 1)")
        assertThat(game.hasUndefendedCards()).isTrue()
    }
}
