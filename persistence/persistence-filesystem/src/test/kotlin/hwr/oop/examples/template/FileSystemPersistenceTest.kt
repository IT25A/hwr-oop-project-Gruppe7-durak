package hwr.oop.examples.template

import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GameNotFoundException
import hwr.oop.examples.template.core.PlayerId
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class FileSystemPersistenceTest {
	
	private val fakeFileSystem = FakeFileSystem()
	private val tempDir = "/tmp/template-test/".toPath()
	
	private val sut = FileSystemPersistence(
		FileSystemPersistenceConfiguration(tempDir),
		fakeFileSystem
	)
	
	private fun newGame(): Game =
		Game.create(
			listOf(
				PlayerId("p1"),
				PlayerId("p2")
			)
		)
	
	@Test
	fun `can save and load newly created game`() {
		val game = newGame()
		
		sut.saveGame("game-new", game)
		val loaded = sut.loadGame("game-new")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `can save and load active round game`() {
		val game = newGame()
		
		sut.saveGame("game-active-round", game)
		val loaded = sut.loadGame("game-active-round")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `can save and load game after attack`() {
		val game = newGame()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		
		sut.saveGame("game-after-attack", game)
		val loaded = sut.loadGame("game-after-attack")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `save with same game id overwrites previous state`() {
		val game = newGame()
		
		sut.saveGame("same-id", game)
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		
		sut.saveGame("same-id", game)
		val loaded = sut.loadGame("same-id")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `saved file exists after save`() {
		val game = newGame()
		
		sut.saveGame("file-exists", game)
		
		val path = tempDir.resolve("file-exists.json")
		assertThat(fakeFileSystem.exists(path)).isTrue()
	}
	
	@Test
	fun `saved file is not empty`() {
		val game = newGame()
		
		sut.saveGame("file-not-empty", game)
		
		val path = tempDir.resolve("file-not-empty.json")
		val content = fakeFileSystem.read(path) { readUtf8() }
		
		assertThat(content).isNotBlank()
	}
	
	@Test
	fun `load game not saved throws exception`() {
		assertThatThrownBy {
			sut.loadGame("missing-game")
		}.isInstanceOf(GameNotFoundException::class.java)
	}
	
	@Test
	fun `can save and load game after end round`() {
		val game = newGame()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		game.endRound()
		
		sut.saveGame("game-after-end-round", game)
		val loaded = sut.loadGame("game-after-end-round")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `loaded game can continue playing after load`() {
		val game = newGame()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		
		sut.saveGame("continue-after-load", game)
		val loaded = sut.loadGame("continue-after-load")
		
		assertThatCode {
			loaded.endRound()
		}.doesNotThrowAnyException()
	}
	
	@Test
	fun `can load overwritten game and continue playing`() {
		val game = newGame()
		sut.saveGame("overwrite-and-play", game)
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		sut.saveGame("overwrite-and-play", game)
		
		val loaded = sut.loadGame("overwrite-and-play")
		
		assertThat(loaded.isRoundActive()).isTrue()
		assertThat(loaded.getRoundCardPairings()).isNotEmpty()
		
		assertThatCode {
			loaded.endRound()
		}.doesNotThrowAnyException()
	}
	
	@Test
	fun `can save and load game twice in a row`() {
		val game = newGame()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		
		sut.saveGame("double-roundtrip", game)
		val loadedOnce = sut.loadGame("double-roundtrip")
		
		sut.saveGame("double-roundtrip", loadedOnce)
		val loadedTwice = sut.loadGame("double-roundtrip")
		
		assertThat(loadedTwice)
			.usingRecursiveComparison()
			.isEqualTo(loadedOnce)
	}
	
	@Test
	fun `loading same game twice returns same state`() {
		val game = newGame()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		
		sut.saveGame("load-twice", game)
		
		val loadedOnce = sut.loadGame("load-twice")
		val loadedTwice = sut.loadGame("load-twice")
		
		assertThat(loadedOnce)
			.usingRecursiveComparison()
			.isEqualTo(loadedTwice)
	}
	
	@Test
	fun `can overwrite saved file with fresh new game`() {
		val firstGame = newGame()
		
		val attacker = firstGame.getAttacker()
		val card = firstGame.getPlayerHand(attacker)!!.cards().first()
		firstGame.attackWithCard(card)
		
		sut.saveGame("replace-with-new-game", firstGame)
		
		val secondGame = newGame()
		sut.saveGame("replace-with-new-game", secondGame)
		
		val loaded = sut.loadGame("replace-with-new-game")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(secondGame)
	}
	
	@Test
	fun `loaded ended round game can continue with next round`() {
		val game = newGame()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		game.endRound()
		
		sut.saveGame("next-round-after-load", game)
		val loaded = sut.loadGame("next-round-after-load")
		
		assertThat(loaded.isRoundActive()).isTrue()
		
		assertThatCode {
			val nextAttacker = loaded.getAttacker()
			val nextCard = loaded.getPlayerHand(nextAttacker)!!.cards().first()
			loaded.attackWithCard(nextCard)
		}.doesNotThrowAnyException()
	}
	
	@Test
	fun `save creates directory when it does not exist`() {
		val nonExistingDir = "/tmp/non-existing-template-dir/".toPath()
		val persistence = FileSystemPersistence(
			FileSystemPersistenceConfiguration(nonExistingDir),
			fakeFileSystem
		)
		
		val game = newGame()
		persistence.saveGame("creates-dir", game)
		
		assertThat(fakeFileSystem.exists(nonExistingDir)).isTrue()
		assertThat(fakeFileSystem.exists(nonExistingDir.resolve("creates-dir.json"))).isTrue()
	}
	
	@AfterEach
	fun tearDown() {
		fakeFileSystem.checkNoOpenFiles()
	}
}