package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import hwr.oop.examples.template.core.GamePersistence
import hwr.oop.examples.template.core.PlayerId

class GetPlayerHandCommand(
	private val persistence: GamePersistence? = null,
) : CliktCommand(name = "getPlayerHand") {
	private val gameId by requireObject<String>()
	private val playerId by option("--player-id", help = "The ID of the player to view the hand for.").required()
	
	override fun run() {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val game = gamePersistence.loadGame(gameId)
		val hand = game.getPlayerHand(PlayerId(playerId))
		
		if (hand == null) {
			println("Player $playerId not found in game $gameId")
			return
		}
		
		val cards = hand.cards()
		if (cards.isEmpty()) {
			println("Player $playerId has no cards")
			return
		}
		
		println("=== PLAYER HAND ===")
		println("Player: $playerId")
		println("Cards (${cards.size}):")
		cards.forEach { card ->
			println("  ${card.suit()}:${card.rank()}")
		}
	}
}
