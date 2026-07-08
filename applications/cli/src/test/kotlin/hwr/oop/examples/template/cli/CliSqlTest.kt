package hwr.oop.examples.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.core.subcommands
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import hwr.oop.examples.template.SqlPersistence
import hwr.oop.examples.template.core.Bout
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.PlayerId
import hwr.oop.examples.template.core.Rank
import hwr.oop.examples.template.core.Suit
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID

@Testcontainers(disabledWithoutDocker = true)
class CliSqlTest {
	
	companion object {
		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer("postgres:17-alpine")
	}
	
	private lateinit var persistence: SqlPersistence
	private lateinit var dataSource: HikariDataSource
	
	@BeforeEach
	fun setUp() {
		val config = HikariConfig().apply {
			jdbcUrl = postgres.jdbcUrl
			username = postgres.username
			password = postgres.password
		}
		dataSource = HikariDataSource(config)
		persistence = SqlPersistence(dataSource)
	}
	
	@AfterEach
	fun tearDown() {
		if (::dataSource.isInitialized) {
			dataSource.close()
		}
	}
	
	private fun saveGame(players: List<String>): String {
		val gameId = UUID.randomUUID().toString()
		val game = Game.create(players.map { PlayerId(it) })
		persistence.saveGame(gameId, game)
		return gameId
	}
	
	private fun rootWith(vararg commands: CliktCommand): CliktCommand =
		ExampleBaseCommand().subcommands(*commands)
	
	private fun rootWithGameId(vararg commands: CliktCommand): CliktCommand =
		ExampleBaseCommand().subcommands(
			OnGameIdCommand().subcommands(*commands)
		)
	
	@Test
	fun `parseCard parses valid sql card`() {
		val card = parseCard("HEARTS:ACE")
		
		assertThat(card.suit()).isEqualTo(Suit.HEARTS)
		assertThat(card.rank()).isEqualTo(Rank.ACE)
	}
	
	@Test
	fun `parseCard parses another sql card`() {
		val card = parseCard("SPADES:TEN")
		
		assertThat(card.suit()).isEqualTo(Suit.SPADES)
		assertThat(card.rank()).isEqualTo(Rank.TEN)
	}
	
	@Test
	fun `parseCard fails on invalid sql card`() {
		assertThatThrownBy {
			parseCard("INVALID")
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
	
	@Test
	fun `example base command can run with sql`() {
		ExampleBaseCommand().parse(emptyList())
	}
	
	@Test
	fun `getGame loads persisted sql game`() {
		val gameId = saveGame(listOf("p1", "p2"))
		
		rootWithGameId(GetGameCommand(persistence))
			.parse(listOf("onGameID", gameId, "getGame"))
		
		val loaded = persistence.loadGame(gameId)
		assertThat(loaded.getPlayers()).hasSize(2)
	}
	
	@Test
	fun `startGame with two players succeeds in sql`() {
		rootWith(StartGameCommand(persistence))
			.parse(listOf("startGame", "--player-id", "p1", "--player-id", "p2"))
	}
	
	@Test
	fun `startGame with three players succeeds in sql`() {
		rootWith(StartGameCommand(persistence))
			.parse(listOf("startGame", "--player-id", "p1", "--player-id", "p2", "--player-id", "p3"))
	}
	
	@Test
	fun `startGame with one player fails in sql`() {
		assertThatThrownBy {
			rootWith(StartGameCommand(persistence))
				.parse(listOf("startGame", "--player-id", "p1"))
		}.isInstanceOf(Exception::class.java)
	}
	
	@Test
	fun `pass command saves modified sql game`() {
		val gameId = saveGame(listOf("p1", "p2", "p3"))
		val before = persistence.loadGame(gameId).getSupplyPasses()
		
		rootWithGameId(PassCommand(persistence))
			.parse(listOf("onGameID", gameId, "pass", "--player-id", "p1"))
		
		val after = persistence.loadGame(gameId).getSupplyPasses()
		
		assertThat(before).doesNotContain(PlayerId("p1"))
		assertThat(after).contains(PlayerId("p1"))
	}
	
	@Test
	fun `pass command fails for defender in sql`() {
		val gameId = saveGame(listOf("p1", "p2", "p3"))
		val defender = persistence.loadGame(gameId).getDefender().asString()
		
		assertThatThrownBy {
			rootWithGameId(PassCommand(persistence))
				.parse(listOf("onGameID", gameId, "pass", "--player-id", defender))
		}.isInstanceOf(Exception::class.java)
	}
	
	@Test
	fun `supply command as attacker changes sql table`() {
		val gameId = saveGame(listOf("p1", "p2"))
		val attacker = persistence.loadGame(gameId).getAttacker().asString()
		val attackerCard = persistence.loadGame(gameId)
			.getPlayerHand(PlayerId(attacker))!!
			.cards()
			.first()
		
		rootWithGameId(SupplyCommand(persistence)).parse(
			listOf(
				"onGameID", gameId,
				"supply",
				"--player-id", attacker,
				"--card", "${attackerCard.suit()}:${attackerCard.rank()}"
			)
		)
		
		val reloaded = persistence.loadGame(gameId)
		assertThat(reloaded.getRoundCardPairings()).isNotEmpty
	}
	
	@Test
	fun `attack command executes and saves sql game`() {
		val gameId = saveGame(listOf("p1", "p2"))
		val attacker = persistence.loadGame(gameId).getAttacker().asString()
		val attackerCard = persistence.loadGame(gameId)
			.getPlayerHand(PlayerId(attacker))!!
			.cards()
			.first()
		
		rootWithGameId(AttackCommand(persistence)).parse(
			listOf(
				"onGameID", gameId,
				"attack",
				"--player-id", attacker,
				"--card", "${attackerCard.suit()}:${attackerCard.rank()}"
			)
		)
		
		val reloaded = persistence.loadGame(gameId)
		assertThat(reloaded.getRoundCardPairings()).isNotEmpty
	}
	
	@Test
	fun `defend command saves defended sql card`() {
		val gameId = saveGame(listOf("p1", "p2"))
		val game = persistence.loadGame(gameId)
		val attacker = game.getAttacker()
		val defender = game.getDefender()
		
		val attackerHand = game.getPlayerHand(attacker)!!.cards()
		val defenderHand = game.getPlayerHand(defender)!!.cards()
		val probeBout = Bout(
			attacker = game.getPlayerHand(attacker)!!,
			defender = game.getPlayerHand(defender)!!,
			trump = game.getTrumpSuit()
		)
		
		val attackAndDefense = attackerHand.firstNotNullOfOrNull { attackCard ->
			val defenseCard = defenderHand.firstOrNull { candidate ->
				probeBout.cardBeats(attackCard, candidate)
			}
			if (defenseCard != null) attackCard to defenseCard else null
		}
		
		if (attackAndDefense == null) return
		
		val (attackCard, defenseCard) = attackAndDefense
		
		game.attackWithCard(attackCard)
		persistence.saveGame(gameId, game)
		
		rootWithGameId(DefendCommand(persistence)).parse(
			listOf(
				"onGameID", gameId,
				"defend",
				"--player-id", defender.asString(),
				"--attack-card", "${attackCard.suit()}:${attackCard.rank()}",
				"--defense-card", "${defenseCard.suit()}:${defenseCard.rank()}"
			)
		)
		
		val reloaded = persistence.loadGame(gameId)
		assertThat(reloaded.getRoundCardPairings()[attackCard]).isEqualTo(defenseCard)
	}
	
	@Test
	fun `multiple sql games are saved and loaded independently`() {
		val gameId1 = UUID.randomUUID().toString()
		val game1 = Game.create(listOf(PlayerId("p1"), PlayerId("p2")))
		persistence.saveGame(gameId1, game1)
		
		val gameId2 = UUID.randomUUID().toString()
		val game2 = Game.create(listOf(PlayerId("p3"), PlayerId("p4")))
		persistence.saveGame(gameId2, game2)
		
		val loaded1 = persistence.loadGame(gameId1)
		val loaded2 = persistence.loadGame(gameId2)
		
		assertThat(loaded1.getPlayers()).hasSize(2)
		assertThat(loaded2.getPlayers()).hasSize(2)
	}
	
	@Test
	fun `sql game can be repeatedly saved and loaded`() {
		val gameId = UUID.randomUUID().toString()
		val game = Game.create(listOf(PlayerId("p1"), PlayerId("p2")))
		
		repeat(5) {
			persistence.saveGame(gameId, game)
			val loaded = persistence.loadGame(gameId)
			assertThat(loaded.getPlayers()).hasSize(2)
		}
	}
}