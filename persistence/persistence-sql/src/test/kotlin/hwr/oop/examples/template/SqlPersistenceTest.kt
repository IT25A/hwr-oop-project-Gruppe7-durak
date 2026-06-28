package hwr.oop.examples.template

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GameNotFoundException
import hwr.oop.examples.template.core.PlayerId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Disabled("Requires Docker")
@Testcontainers
class SqlPersistenceTest {
	
	companion object {
		@Container
		@JvmStatic
		val postgres = PostgreSQLContainer("postgres:17-alpine")
	}
	
	private lateinit var sut: SqlPersistence
	private lateinit var dataSource: HikariDataSource
	
	private fun newGame(): Game =
		Game.create(
			listOf(
				PlayerId("p1"),
				PlayerId("p2")
			)
		)
	
	@BeforeEach
	fun setUp() {
		val config = HikariConfig().apply {
			jdbcUrl = postgres.jdbcUrl
			username = postgres.username
			password = postgres.password
		}
		dataSource = HikariDataSource(config)
		sut = SqlPersistence(dataSource)
	}
	
	@AfterEach
	fun tearDown() {
		if (::dataSource.isInitialized) {
			dataSource.close()
		}
	}
	
	@Test
	fun `can create persistence from jdbc url and credentials`() {
		val sut2 = SqlPersistence(postgres.jdbcUrl, postgres.username, postgres.password)
		val game = newGame()
		
		sut2.saveGame("constructor-test", game)
		val loaded = sut2.loadGame("constructor-test")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
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
	fun `can save and load game after start round`() {
		val game = newGame()
		game.startRound()
		
		sut.saveGame("game-start-round", game)
		val loaded = sut.loadGame("game-start-round")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `can save and load game after attack`() {
		val game = newGame()
		game.startRound()
		
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
	fun `can save and load game after end round`() {
		val game = newGame()
		game.startRound()
		
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
	fun `load game not saved throws exception`() {
		assertThatThrownBy {
			sut.loadGame("missing-game")
		}.isInstanceOf(GameNotFoundException::class.java)
	}
	
	@Test
	fun `can save and load game after two attacks`() {
		val game = newGame()
		game.startRound()
		
		val attacker = game.getAttacker()
		val hand = game.getPlayerHand(attacker)!!
		val firstCard = hand.cards().first()
		game.attackWithCard(firstCard)
		
		val secondAttacker = game.getAttacker()
		val secondHand = game.getPlayerHand(secondAttacker)!!
		val secondCard = secondHand.cards().first()
		game.attackWithCard(secondCard)
		
		sut.saveGame("game-two-attacks", game)
		val loaded = sut.loadGame("game-two-attacks")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `can save and load game after attack and end round`() {
		val game = newGame()
		game.startRound()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		game.endRound()
		
		sut.saveGame("game-attack-end-round", game)
		val loaded = sut.loadGame("game-attack-end-round")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `can save and load game after multiple round transitions`() {
		val game = newGame()
		game.startRound()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		game.endRound()
		
		game.startRound()
		val nextAttacker = game.getAttacker()
		val nextCard = game.getPlayerHand(nextAttacker)!!.cards().first()
		game.attackWithCard(nextCard)
		
		sut.saveGame("game-multiple-transitions", game)
		val loaded = sut.loadGame("game-multiple-transitions")
		
		assertThat(loaded)
			.usingRecursiveComparison()
			.isEqualTo(game)
	}
	
	@Test
	fun `loading same game twice returns equal state`() {
		val game = newGame()
		game.startRound()
		
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
	fun `saved game can continue after first load`() {
		val game = newGame()
		game.startRound()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		
		sut.saveGame("continue-after-first-load", game)
		val loaded = sut.loadGame("continue-after-first-load")
		
		assertThatCode {
			loaded.endRound()
		}.doesNotThrowAnyException()
	}
	
	@Test
	fun `loaded game can start next round`() {
		val game = newGame()
		game.startRound()
		
		val attacker = game.getAttacker()
		val card = game.getPlayerHand(attacker)!!.cards().first()
		game.attackWithCard(card)
		game.endRound()
		
		sut.saveGame("start-next-round", game)
		val loaded = sut.loadGame("start-next-round")
		
		assertThatCode {
			loaded.startRound()
		}.doesNotThrowAnyException()
	}
	
	@Test
	fun `can save two different games independently`() {
		val gameOne = newGame()
		gameOne.startRound()
		val attackerOne = gameOne.getAttacker()
		val cardOne = gameOne.getPlayerHand(attackerOne)!!.cards().first()
		gameOne.attackWithCard(cardOne)
		
		val gameTwo = newGame()
		
		sut.saveGame("game-one", gameOne)
		sut.saveGame("game-two", gameTwo)
		
		val loadedOne = sut.loadGame("game-one")
		val loadedTwo = sut.loadGame("game-two")
		
		assertThat(loadedOne)
			.usingRecursiveComparison()
			.isEqualTo(gameOne)
		
		assertThat(loadedTwo)
			.usingRecursiveComparison()
			.isEqualTo(gameTwo)
	}
	
	@Test
	fun `saving game with existing id throws exception`() {
		val game = newGame()
		sut.saveGame("duplicate-id", game)
		
		assertThatThrownBy {
			sut.saveGame("duplicate-id", game)
		}.isInstanceOf(Exception::class.java)
	}
	
	@Test
	fun `loading unknown game throws not found exception`() {
		assertThatThrownBy {
			sut.loadGame("does-not-exist")
		}.isInstanceOf(GameNotFoundException::class.java)
	}
}