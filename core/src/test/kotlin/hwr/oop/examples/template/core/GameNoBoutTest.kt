package hwr.oop.examples.template.core

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions

class GameNoBoutTest {

    private val p1 = PlayerId("P1")
    private val p2 = PlayerId("P2")
    private val p3 = PlayerId("P3")

    private val spade6 = Card(Suit.SPADES, Rank.SIX)
    private val club6 = Card(Suit.CLUBS, Rank.SIX)
    private val heart8 = Card(Suit.HEARTS, Rank.EIGHT)

    @Test
    fun `attackWithCard throws NoActiveBoutException when round is active but bout is missing`() {
        val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(emptyList(), p2))
        val game = Game(hands, listOf(p1, p2), MutableDeck(mutableListOf()), roundActive = false)

        val field = Game::class.java.getDeclaredField("roundActive")
        field.isAccessible = true
        field.setBoolean(game, true)

        Assertions.assertThrows(NoActiveBoutException::class.java) { game.attackWithCard(spade6) }
    }

    @Test
    fun `joinAttack throws NoActiveBoutException when round is active but bout is missing`() {
        val hands = mapOf(
            p1 to PlayerHand.create(listOf(spade6), p1),
            p2 to PlayerHand.create(listOf(club6), p2),
            p3 to PlayerHand.create(listOf(heart8), p3)
        )
        val game = Game(hands, listOf(p1, p2, p3), MutableDeck(mutableListOf()), roundActive = false)

        val field = Game::class.java.getDeclaredField("roundActive")
        field.isAccessible = true
        field.setBoolean(game, true)

        Assertions.assertThrows(NoActiveBoutException::class.java) { game.joinAttack(p3, heart8) }
    }

    @Test
    fun `defendCard throws NoActiveBoutException when round is active but bout is missing`() {
        val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(listOf(club6), p2))
        val game = Game(hands, listOf(p1, p2), MutableDeck(mutableListOf()), roundActive = false)

        val field = Game::class.java.getDeclaredField("roundActive")
        field.isAccessible = true
        field.setBoolean(game, true)

        Assertions.assertThrows(NoActiveBoutException::class.java) { game.defendCard(spade6, club6) }
    }

    @Test
    fun `endRound throws NoActiveBoutException when round is active but bout is missing`() {
        val hands = mapOf(p1 to PlayerHand.create(listOf(spade6), p1), p2 to PlayerHand.create(listOf(club6), p2))
        val game = Game(hands, listOf(p1, p2), MutableDeck(mutableListOf()), roundActive = false)

        val field = Game::class.java.getDeclaredField("roundActive")
        field.isAccessible = true
        field.setBoolean(game, true)

        Assertions.assertThrows(NoActiveBoutException::class.java) { game.endRound() }
    }
}
