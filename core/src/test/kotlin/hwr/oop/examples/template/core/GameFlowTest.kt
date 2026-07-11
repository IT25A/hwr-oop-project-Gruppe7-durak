package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GameFlowTest {
	@Test
	fun `attacker can play card during round`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)?.cards()?.first() ?: return
		
		game.attackWithCard(card)
		assertThat(game.getRoundCardPairings()).containsKey(card)
	}
	
	@Test
	fun `defender can beat card when possible`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		val attackCard = game.getPlayerHand(game.getAttacker())?.cards()?.first() ?: return
		game.attackWithCard(attackCard)
		
		val trumpSuit = game.getTrumpSuit()
		val defenderHand = game.getPlayerHand(game.getDefender())
		
		val defendCard = defenderHand?.cards()?.find { def ->
			if (def.suit() == attackCard.suit()) {
				def.getCardValue(def.rank()) > attackCard.getCardValue(attackCard.rank())
			} else {
				def.suit() == trumpSuit
			}
		}
		
		if (defendCard != null) {
			game.defendCard(attackCard, defendCard)
			assertThat(game.getRoundCardPairings()[attackCard]).isEqualTo(defendCard)
		}
	}
	
	@Test
	fun `defender cannot beat with weaker card`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		
		val attackerHand = game.getPlayerHand(game.getAttacker())
		val defenderHand = game.getPlayerHand(game.getDefender())
		
		val attackCard = attackerHand?.cards()?.maxByOrNull { it.getCardValue(it.rank()) }
		val defendCard = defenderHand?.cards()?.minByOrNull { it.getCardValue(it.rank()) }
		
		if (attackCard != null && defendCard != null && attackCard.suit() == defendCard.suit()) {
			game.attackWithCard(attackCard)
			org.junit.jupiter.api.assertThrows<CardDoesNotBeatAttackingCardException> {
				game.defendCard(attackCard, defendCard)
			}
		}
	}
	
	@Test
	fun `hasUndefendedCards detects unbeaten attacks`() {
		val p1 = PlayerId("P1")
		val p2 = PlayerId("P2")
		val game = Game.create(listOf(p1, p2))
		
		val attackerCard = game.getPlayerHand(game.getAttacker())?.cards()?.first()
		if (attackerCard != null) {
			game.attackWithCard(attackerCard)
			assertThat(game.hasUndefendedCards()).isTrue()
		}
	}
	
	@Test
	fun `fully defended round is true when no attacks`() {
		val game = Game.create(listOf(PlayerId("P1"), PlayerId("P2")))
		// no attacks
		assertThat(game.isRoundFullyDefended()).isTrue()
	}
}