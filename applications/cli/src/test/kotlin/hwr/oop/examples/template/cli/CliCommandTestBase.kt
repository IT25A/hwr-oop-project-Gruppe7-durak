package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import hwr.oop.examples.template.FileSystemPersistence
import hwr.oop.examples.template.FileSystemPersistenceConfiguration
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.PlayerId
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.util.UUID

abstract class CliCommandTestBase {
	
	protected val fakeFileSystem = FakeFileSystem()
	protected val tempDir = "/tmp/cli-fs-test".toPath()
	protected lateinit var persistence: FileSystemPersistence
	
	@BeforeEach
	fun setUpBase() {
		fakeFileSystem.createDirectories(tempDir)
		persistence = FileSystemPersistence(
			FileSystemPersistenceConfiguration(tempDir),
			fakeFileSystem
		)
	}
	
	@AfterEach
	fun tearDownBase() {
		fakeFileSystem.checkNoOpenFiles()
	}
	
	protected fun saveGame(players: List<String>): String {
		val gameId = UUID.randomUUID().toString()
		val game = Game.create(players.map { PlayerId(it) })
		persistence.saveGame(gameId, game)
		return gameId
	}
	
	protected fun rootWith(vararg commands: CliktCommand): CliktCommand =
		ExampleBaseCommand().subcommands(*commands)
	
	protected fun rootWithGameId(vararg commands: CliktCommand): CliktCommand =
		ExampleBaseCommand().subcommands(
			OnGameIdCommand().subcommands(*commands)
		)
}