package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameJoinTest {
	
	@Test
	fun `joinAttack without active bout throws`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val hands = mapOf(
			players[0] to PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), players[0]),
			players[1] to PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), players[1]),
			players[2] to PlayerHand.create(listOf(Card(Suit.CLUBS, Rank.SIX)), players[2])
		)
		val game = Game(hands, players, Deck.createRandomDeck().toMutableDeck())
		
		val joinCard = hands[players[2]]?.cards()?.first() ?: return
		assertThrows<NoActiveRoundException> { game.joinAttack(players[2], joinCard) }
	}
	
	@Test
	fun `other players can join attack in 3-player game`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		val third = players.first { it != attacker && it != defender }
		
		val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		val joinerCard = game.getPlayerHand(third)?.cards()?.firstOrNull { it.rank() == attackCard.rank() } ?: return
		
		game.attackWithCard(attackCard)
		game.joinAttack(third, joinerCard)
		
		assertThat(game.getCurrentRoundAttackers()).contains(third)
		assertThat(game.getRoundCardPairings().size).isEqualTo(2)
	}
	
	@Test
	fun `attacker cannot join own attack and defender cannot join`() {
		val players = listOf(PlayerId("P1"), PlayerId("P2"), PlayerId("P3"))
		val game = Game.create(players)
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		val attackCard = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val secondCard = game.getPlayerHand(attacker)?.cards()?.firstOrNull { it != attackCard } ?: return
		Assertions.assertThrows(AttackerAndDefenderCanNotJoinAttackException::class.java) {
			game.joinAttack(attacker, secondCard)
		}
		val defenderCard = game.getPlayerHand(defender)?.cards()?.first() ?: return
		Assertions.assertThrows(AttackerAndDefenderCanNotJoinAttackException::class.java) {
			game.joinAttack(defender, defenderCard)
		}
	}
	
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
		val p2Hand = PlayerHand.create(
			listOf(Card(Suit.SPADES, Rank.JACK), Card(Suit.SPADES, Rank.NINE)),
			p2
		) // Only one card - can't defend both attacks
		val p3Hand = PlayerHand.create(listOf(card2), p3) // Will receive cards from replenish
		val p4Hand = PlayerHand.create(listOf(card3), p4)
		
		val hands = mapOf(p1 to p1Hand, p2 to p2Hand, p3 to p3Hand, p4 to p4Hand)
		val game = Game(hands, listOf(p1, p2, p3, p4), deck = MutableDeck(mutableListOf()), roundActive = true)
		
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		
		
		game.attackWithCard(attackCard)
		
		game.joinAttack(p3, card2)
		
		game.joinAttack(p4, card3)
		
		assertThat(game.getCurrentRoundAttackers()).contains(p3)
		assertThat(game.getCurrentRoundAttackers()).contains(p4)
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
		val game = Game(hands, listOf(p1, p2, p3), Deck.createRandomDeck().toMutableDeck(), roundActive = true)
		
		
		val attackCard = attackerHand.cards().first()
		game.attackWithCard(attackCard)
		
		// joiner has rank EIGHT, attack rank is SIX -> should be rejected
		Assertions.assertThrows(RankNotOnTableException::class.java) {
			game.joinAttack(p3, joinerHand.cards().first())
		}
		
		// try with a card not in player's hand
		val fakeCard = Card(Suit.SPADES, Rank.NINE)
		Assertions.assertThrows(JoinerDoesNotHaveCardException::class.java) {
			game.joinAttack(p3, fakeCard)
		}
		
		// Make a joiner with a correct rank and succeed, then second attempt should be false
		val joiner2Hand =
			PlayerHand.create(listOf(Card(Suit.DIAMONDS, Rank.SIX), Card(Suit.CLUBS, Rank.SIX)), PlayerId("P4"))
		val p4 = PlayerId("P4")
		
		// create a fresh game for this part (use fresh attacker hand instance)
		val attackerHand2 = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand2 = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.CLUBS, Rank.EIGHT)), p2)
		val handsPart = mapOf(p1 to attackerHand2, p2 to defenderHand2, p4 to joiner2Hand)
		val game2 = Game(handsPart, listOf(p1, p2, p4), Deck.createRandomDeck().toMutableDeck(), roundActive = true)
		game2.attackWithCard(attackerHand2.cards().first())
		game2.joinAttack(p4, joiner2Hand.cards().first())
		
		Assertions.assertThrows(AttackerCanNotJoinHisAttackException::class.java) {
			game2.joinAttack(p4, joiner2Hand.cards().first())
		}
	}
	
	@Test
	fun `joinAttack throws JoinerNotFoundException when player is missing`() {
		val attacker = PlayerId("P1")
		val defender = PlayerId("P2")
		val joiner = PlayerId("P3")
		
		val attackerHand = PlayerHand.create(
			listOf(Card(Suit.SPADES, Rank.SIX)), id = attacker
		)
		val defenderHand = PlayerHand.create(
			emptyList(),
			id = defender
		)
		
		val hands = mapOf(
			attacker to attackerHand,
			defender to defenderHand
		)
		
		val game = Game(
			handsOfPlayers = hands,
			players = listOf(attacker, defender, joiner),
			deck = MutableDeck(cards = mutableListOf()),
			roundActive = true
		)
		
		
		val card = attackerHand.cards().first()
		
		val ex = assertThrows<JoinerNotFoundException> {
			game.joinAttack(joiner, card)
		}
		
		assertThat(ex.message).isEqualTo("Player not found")
	}
}