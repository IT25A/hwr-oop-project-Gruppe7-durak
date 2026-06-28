package hwr.oop.examples.template

import com.zaxxer.hikari.HikariDataSource
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GameNotFoundException
import hwr.oop.examples.template.core.GamePersistence
import liquibase.Liquibase
import liquibase.Scope
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.logging.core.NoOpLogService
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.ui.LoggerUIService
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource

class SqlPersistence(
	private val dataSource: DataSource,
) : GamePersistence {
	
	constructor(jdbcUrl: String, username: String, password: String) : this(
		HikariDataSource().apply {
			setJdbcUrl(jdbcUrl)
			setUsername(username)
			setPassword(password)
		}
	)
	
	init {
		runLiquibaseMigrations()
		Database.connect(dataSource)
	}
	
	private fun runLiquibaseMigrations() {
		System.setProperty("liquibase.command.update.showSummary", "OFF")
		val scopeAttrs = mapOf(
			Scope.Attr.logService.name to NoOpLogService(),
			Scope.Attr.ui.name to LoggerUIService(),
		)
		Scope.child(scopeAttrs) {
			dataSource.connection.use { connection ->
				val database = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(JdbcConnection(connection))
				Liquibase(
					"db/changelog/db.changelog-master.yaml",
					ClassLoaderResourceAccessor(),
					database
				).update("")
			}
		}
	}
	
	override fun saveGame(gameId: String, game: Game) {
		transaction {
			DurakGamesTable.insert {
				it[DurakGamesTable.id] = gameId
				it[DurakGamesTable.game] = game
			}
			Unit
		}
	}
	
	override fun loadGame(gameId: String): Game {
		val result = transaction {
			DurakGamesTable
				.select(DurakGamesTable.game)
				.where { DurakGamesTable.id eq gameId }
				.map { it[DurakGamesTable.game] }
				.firstOrNull()
		}
		
		return result ?: throw GameNotFoundException(gameId)
	}
}