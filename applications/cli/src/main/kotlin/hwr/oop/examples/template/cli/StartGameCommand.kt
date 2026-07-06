package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GamePersistence
import hwr.oop.examples.template.core.PlayerId
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import java.util.UUID

class StartGameCommand(
	private val persistence: GamePersistence? = null,
) : CliktCommand(name = "startGame") {
	private val playerIds by option(
		"--player-id",
		help = "ID of a player joining the game. Pass multiple times for each player (2–6 total)."
	).multiple(required = true)
	
	override fun run() {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val game = Game.create(playerIds.map { PlayerId(it) })
		val gameId = UUID.randomUUID().toString()
		gamePersistence.saveGame(gameId, game)
		println(gameId)
	}
}
