package hwr.oop.examples.template.core

interface GamePersistence {
	fun saveGame(gameId: String, game: Game)
	
	@Throws(GameNotFoundException::class)
	fun loadGame(gameId: String): Game
}