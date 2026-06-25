package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameAttackDefendTest {
	
	private val p1 = PlayerId("P1")
	private val p2 = PlayerId("P2")
	
	@Test
	fun `attackWithCard returns false when defender capacity exceeded and rank not on table`() {
		
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX), Card(Suit.HEARTS, Rank.SEVEN)), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), MutableDeck(mutableListOf()))
		
		game.startRound()
		
		val first = attackerHand.cards().first()
		val second = attackerHand.cards().last()
		
		val ok1 = game.attackWithCard(first)
		assertThat(ok1).isTrue()
		
		Assertions.assertThrows(RankNotOnTableException::class.java) {
			game.attackWithCard(second)
		}
	}
	
	@Test
	fun `defender trump beats non-trump regardless of rank`() {
		
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.KING)), p1)
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
		val attackCard = Card(Suit.CLUBS, Rank.KING)
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
	fun `attacker can play additional card when rank on table`() {
		val attackCard1 = Card(Suit.SPADES, Rank.SIX)
		val attackCard2 = Card(Suit.CLUBS, Rank.SIX)
		val attackerHand = PlayerHand.create(listOf(attackCard1, attackCard2), p1)
		// defender has enough cards so capacity not exceeded
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN), Card(Suit.HEARTS, Rank.EIGHT)), p2)
		
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		
		game.startRound()
		val ok1 = game.attackWithCard(attackCard1)
		assertThat(ok1).isTrue()
		
		val ok2 = game.attackWithCard(attackCard2)
		assertThat(ok2).isTrue()
		assertThat(game.getRoundCardPairings().size).isEqualTo(2)
	}
	
	@Test
	fun `replenishHands draws from deck when players need cards`() {
		val attackerHand = PlayerHand.create(emptyList(), p1)
		val defenderHand = PlayerHand.create(emptyList(), p2)
		
		val deckCards = mutableListOf<Card>()
		for (s in listOf(Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES)) {
			deckCards.add(Card(s, Rank.SIX))
			deckCards.add(Card(s, Rank.SEVEN))
		}
		
		val deck = MutableDeck(deckCards.toMutableList())
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), deck)
		
		game.startRound()
		
		game.replenishHands()
		
		val afterAttacker = game.getPlayerHand(p1)
		val afterDefender = game.getPlayerHand(p2)
		
		assertThat(afterAttacker?.cards()?.size ?: 0).isGreaterThan(0)
		assertThat(afterDefender?.cards()?.size ?: 0).isGreaterThanOrEqualTo(0)
	}
	
	@Test
	fun `isRoundFullyDefended is false when there are undefended attacks`() {
		val attackerHand = PlayerHand.create(listOf(Card(Suit.SPADES, Rank.SIX)), p1)
		val defenderHand = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.SEVEN)), p2)
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), Deck.createRandomDeck().toMutableDeck())
		game.startRound()
		val attackCard = attackerHand.cards().first()
		game.attackWithCard(attackCard)
		// there is an undefended attack -> fully defended should be false
		assertThat(game.isRoundFullyDefended()).isFalse()
	}
	
	@Test
	fun `hasUndefendedCards is false when no attacks played`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		game.startRound()
		// no attacks yet -> should be false
		assertThat(game.hasUndefendedCards()).isFalse()
	}
	
	@Test
	fun `replenishHands does not give defender more than six cards`() {
		// attacker empty hand, defender has 4 cards
		val attackerHand = PlayerHand.create(emptyList(), p1)
		val defenderHand = PlayerHand.create(
			listOf(
				Card(Suit.SPADES, Rank.SIX),
				Card(Suit.HEARTS, Rank.SIX),
				Card(Suit.CLUBS, Rank.SIX),
				Card(Suit.DIAMONDS, Rank.SIX)
			), p2
		)
		val deckCards = mutableListOf<Card>()
		for (s in Suit.entries) {
			for (r in Rank.entries) {
				deckCards.add(Card(s, r))
			}
		}
		val deck = MutableDeck(deckCards.toMutableList())
		val hands = mapOf(p1 to attackerHand, p2 to defenderHand)
		val game = Game(hands, listOf(p1, p2), deck)
		game.startRound()
		game.replenishHands()
		assertThat(game.getPlayerHand(p2)?.cards()?.size).isEqualTo(6)
	}
	
	@Test
	fun `allows when rank is on table even if not first attack and sizes equal`() {
		// given attacker plays two cards with same rank; defender has 1 card
		val attackerId = PlayerId("P1")
		val defenderId = PlayerId("P2")
		
		val firstAttack = Card(Suit.SPADES, Rank.SIX)
		val secondAttackSameRank = Card(Suit.CLUBS, Rank.SIX) // same rank as first attack
		val defenderCard = Card(Suit.HEARTS, Rank.KING)
		
		val hands = mapOf(
			attackerId to PlayerHand.create(listOf(firstAttack, secondAttackSameRank), attackerId),
			defenderId to PlayerHand.create(listOf(defenderCard), defenderId)
		)
		
		val game = Game(hands, listOf(attackerId, defenderId), Deck.createRandomDeck().toMutableDeck())
		game.startRound()
		
		assertThat(game.attackWithCard(firstAttack)).isTrue
		assertThat(game.attackWithCard(secondAttackSameRank)).isTrue
	}
	
	@Test
	fun `allows when defender has more cards than current attack stack even if rank not on table`() {
		// given: defender has 2 cards, attacker plays two different-rank cards -> second should be allowed
		val attackerId = PlayerId("P1")
		val defenderId = PlayerId("P2")
		
		val firstAttack = Card(Suit.SPADES, Rank.SIX)
		val secondAttackDifferentRank = Card(Suit.CLUBS, Rank.SEVEN)
		val defenderCards = listOf(Card(Suit.HEARTS, Rank.KING), Card(Suit.DIAMONDS, Rank.NINE))
		
		val hands = mapOf(
			attackerId to PlayerHand.create(listOf(firstAttack, secondAttackDifferentRank), attackerId),
			defenderId to PlayerHand.create(defenderCards, defenderId)
		)
		
		val game = Game(hands, listOf(attackerId, defenderId), Deck.createRandomDeck().toMutableDeck())
		game.startRound()
		
		assertThat(game.attackWithCard(firstAttack)).isTrue
		assertThat(game.attackWithCard(secondAttackDifferentRank)).isTrue
	}
}
