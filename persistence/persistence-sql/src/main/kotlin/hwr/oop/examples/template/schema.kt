package hwr.oop.examples.template

import hwr.oop.examples.template.core.Game
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.jsonb

private val format = Json {
	prettyPrint = false
	isLenient = true
	ignoreUnknownKeys = true
	allowStructuredMapKeys = true
}

object DurakGamesTable : Table("durak_games") {
	val id = varchar("id", 255)
	val game = jsonb<Game>("game", format)
	
	override val primaryKey = PrimaryKey(id, name = "pk_durak_games")
}