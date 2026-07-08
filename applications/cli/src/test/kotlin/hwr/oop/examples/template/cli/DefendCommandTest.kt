package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.parse
import hwr.oop.examples.template.core.Bout
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class DefendCommandTest : CliCommandTestBase() {
	
	@Test
	fun `defend command saves defended card`() {
		val gameId = saveGame(listOf("p1", "p2"))
		val game = persistence.loadGame(gameId)
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		
		val attackerHand = game.getPlayerHand(attacker)!!.cards()
		val defenderHand = game.getPlayerHand(defender)!!.cards()
		val probeBout = Bout(
			attacker = game.getPlayerHand(attacker)!!,
			defender = game.getPlayerHand(defender)!!,
			trump = game.getTrumpSuit()
		)
		
		val attackAndDefense = attackerHand.firstNotNullOfOrNull { attackCard ->
			val defenseCard = defenderHand.firstOrNull { candidate ->
				probeBout.cardBeats(attackCard, candidate)
			}
			if (defenseCard != null) attackCard to defenseCard else null
		}
		
		if (attackAndDefense == null) {
			return
		}
		
		val (attackCard, defenseCard) = attackAndDefense
		
		game.attackWithCard(attackCard)
		persistence.saveGame(gameId, game)
		
		rootWithGameId(DefendCommand(persistence)).parse(
			listOf(
				"onGameID", gameId,
				"defend",
				"--player-id", defender.asString(),
				"--attack-card", "${attackCard.suit()}:${attackCard.rank()}",
				"--defense-card", "${defenseCard.suit()}:${defenseCard.rank()}"
			)
		)
		
		val reloadedAfterDefense = persistence.loadGame(gameId)
		assertThat(reloadedAfterDefense.getRoundCardPairings()[attackCard]).isEqualTo(defenseCard)
	}
	
	@Test
	fun `defend command fails when persistence is not configured`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		assertThatThrownBy {
			rootWithGameId(DefendCommand(null)).parse(
				listOf(
					"onGameID", gameId,
					"defend",
					"--player-id", "p2",
					"--attack-card", "HEARTS:SIX",
					"--defense-card", "HEARTS:ACE"
				)
			)
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `defend command fails for wrong defender`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		assertThatThrownBy {
			rootWithGameId(DefendCommand(persistence)).parse(
				listOf(
					"onGameID", gameId,
					"defend",
					"--player-id", "not-defender",
					"--attack-card", "HEARTS:SIX",
					"--defense-card", "HEARTS:ACE"
				)
			)
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
}