package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import hwr.oop.examples.template.core.GamePersistence

class GetGameCommand(
	private val persistence: GamePersistence? = null,
) : CliktCommand(name = "getGame") {
	private val gameId by requireObject<String>()
	
	override fun run() {
		requireNotNull(persistence) { "Persistence is not configured" }
			.loadGame(gameId).printState()
	}
}
