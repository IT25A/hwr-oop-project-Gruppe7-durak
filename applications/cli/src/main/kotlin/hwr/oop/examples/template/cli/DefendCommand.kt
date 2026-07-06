package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import hwr.oop.examples.template.core.GamePersistence

class DefendCommand(
	private val persistence: GamePersistence? = null,
) : CliktCommand(name = "defend") {
	private val gameId by requireObject<String>()
	private val playerId by option("--player-id", help = "The ID of the defending player.").required()
	private val attackCard by option(
		"--attack-card",
		help = "The attacking card on the table to cover, encoded as a string."
	).required()
	private val defenseCard by option(
		"--defense-card",
		help = "The card from hand used to cover the attack, encoded as a string."
	).required()
	
	override fun run() {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val game = gamePersistence.loadGame(gameId)
		require(playerId == game.getDefender().asString()) {
			"Player $playerId is not the current defender"
		}
		game.defendCard(parseCard(attackCard), parseCard(defenseCard))
		gamePersistence.saveGame(gameId, game)
		game.printState()
	}
}
