package hwr.oop.examples.template.core

class Game(
	private val handsOfPlayers: List<PlayerHand>,
	private val players: List<PlayerId> = handsOfPlayers.map { it.id },
) {
	
}
	
	
