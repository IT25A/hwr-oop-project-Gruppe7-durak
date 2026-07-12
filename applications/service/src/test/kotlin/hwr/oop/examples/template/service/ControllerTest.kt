package hwr.oop.examples.template.service

import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GamePersistence
import hwr.oop.examples.template.core.PlayerId
import hwr.oop.examples.template.core.Suit
import hwr.oop.examples.template.service.model.AttackRequest
import hwr.oop.examples.template.service.model.Card
import hwr.oop.examples.template.service.model.DefendRequest
import hwr.oop.examples.template.service.model.PassRequest
import hwr.oop.examples.template.service.model.StartGameRequest
import hwr.oop.examples.template.service.model.SupplyRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ControllerTest {
	
	private lateinit var persistence: GamePersistence
	private lateinit var controller: Controller
	
	@BeforeEach
	fun setUp() {
		persistence = mockk(relaxed = true)
		controller = Controller(persistence)
	}
	
	@Test
	fun `getGame returns ok with mapped game state`() {
		val gameId = "game-1"
		val game = Game.create(listOf(PlayerId("p1"), PlayerId("p2")))
		every { persistence.loadGame(gameId) } returns game
		
		val response = controller.getGame(gameId)
		
		assertEquals(HttpStatus.OK, response.statusCode)
		assertNotNull(response.body)
		assertEquals(gameId, response.body!!.getGameId())
		assertEquals(2, response.body!!.getPlayerHands().size)
		verify(exactly = 1) { persistence.loadGame(gameId) }
	}
	
	@Test
	fun `getGame throws when persistence is missing`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			Controller(null).getGame("game-1")
		}
		
		assertEquals("Persistence is not configured", ex.message)
	}
	
	@Test
	fun `getGame throws when gameId is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.getGame(null)
		}
		
		assertEquals("gameId is required", ex.message)
	}
	
	@Test
	fun `startGame creates game saves it and returns created`() {
		val savedGameId = slot<String>()
		val savedGame = slot<Game>()
		every { persistence.saveGame(capture(savedGameId), capture(savedGame)) } returns Unit
		
		val response = controller.startGame(
			StartGameRequest(listOf("p1", "p2"))
		)
		
		assertEquals(HttpStatus.CREATED, response.statusCode)
		assertNotNull(response.body)
		assertNotNull(response.body!!.getGameId())
		assertTrue(response.body!!.getGameId()!!.isNotBlank())
		assertTrue(savedGameId.captured.isNotBlank())
		assertEquals(response.body!!.getGameId(), savedGameId.captured)
		assertEquals(2, savedGame.captured.getPlayers().size)
		assertTrue(savedGame.captured.getPlayers().map { it.asString() }.containsAll(listOf("p1", "p2")))
		verify(exactly = 1) { persistence.saveGame(any(), any()) }
	}
	
	@Test
	fun `startGame throws when persistence is missing`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			Controller(null).startGame(StartGameRequest(listOf("p1", "p2")))
		}
		
		assertEquals("Persistence is not configured", ex.message)
	}
	
	@Test
	fun `startGame throws when request is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.startGame(null)
		}
		
		assertEquals("startGameRequest is required", ex.message)
	}
	
	@Test
	fun `attack updates game saves it and returns ok`() {
		val gameId = "game-attack"
		val game = mockk<Game>()
		
		every { persistence.loadGame(gameId) } returns game
		every { game.attackWithCard(any()) } returns game
		every { persistence.saveGame(gameId, game) } returns Unit
		
		every { game.isGameOver() } returns false
		every { game.getDeckCards() } returns emptyList()
		every { game.getTrumpSuit() } returns Suit.HEARTS
		every { game.getAttacker() } returns PlayerId("p1")
		every { game.getDefender() } returns PlayerId("p2")
		every { game.getPlayers() } returns listOf(PlayerId("p1"), PlayerId("p2"))
		every { game.getPlayerHand(any()) } returns null
		every { game.getRoundCardPairings() } returns emptyMap()
		every { game.getSupplyPasses() } returns emptyList()
		
		val response = controller.attack(
			gameId,
			AttackRequest("p1", Card("HEARTS", "SIX"))
		)
		
		assertEquals(HttpStatus.OK, response.statusCode)
		assertNotNull(response.body)
		assertEquals(gameId, response.body!!.getGameId())
		verify(exactly = 1) { persistence.loadGame(gameId) }
		verify(exactly = 1) { game.attackWithCard(any()) }
		verify(exactly = 1) { persistence.saveGame(gameId, game) }
	}
	
	@Test
	fun `attack throws when persistence is missing`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			Controller(null).attack("game-1", AttackRequest("p1", Card("HEARTS", "SIX")))
		}
		
		assertEquals("Persistence is not configured", ex.message)
	}
	
	@Test
	fun `attack throws when gameId is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.attack(null, AttackRequest("p1", Card("HEARTS", "SIX")))
		}
		
		assertEquals("gameId is required", ex.message)
	}
	
	@Test
	fun `attack throws when request is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.attack("game-1", null)
		}
		
		assertEquals("attackRequest is required", ex.message)
	}
	
	@Test
	fun `defend updates game saves it and returns ok for current defender`() {
		val gameId = "game-defend"
		val game = mockk<Game>()
		
		every { persistence.loadGame(gameId) } returns game
		every { game.getDefender() } returns PlayerId("p2")
		every { game.defendCard(any(), any()) } returns game
		every { persistence.saveGame(gameId, game) } returns Unit
		
		every { game.isGameOver() } returns false
		every { game.getDeckCards() } returns emptyList()
		every { game.getTrumpSuit() } returns Suit.HEARTS
		every { game.getAttacker() } returns PlayerId("p1")
		every { game.getPlayers() } returns listOf(PlayerId("p1"), PlayerId("p2"))
		every { game.getPlayerHand(any()) } returns null
		every { game.getRoundCardPairings() } returns emptyMap()
		every { game.getSupplyPasses() } returns emptyList()
		
		val response = controller.defend(
			gameId,
			DefendRequest(
				"p2",
				Card("HEARTS", "SIX"),
				Card("HEARTS", "SEVEN")
			)
		)
		
		assertEquals(HttpStatus.OK, response.statusCode)
		assertNotNull(response.body)
		assertEquals(gameId, response.body!!.getGameId())
		verify(exactly = 1) { persistence.loadGame(gameId) }
		verify(exactly = 1) { persistence.saveGame(gameId, game) }
	}
	
	@Test
	fun `defend throws when player is not current defender`() {
		val gameId = "game-defend"
		val game = Game.create(listOf(PlayerId("p1"), PlayerId("p2")))
		every { persistence.loadGame(gameId) } returns game
		
		val actualDefender = game.getDefender().asString()
		val wrongPlayer = if (actualDefender == "p1") "p2" else "p1"
		
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.defend(
				gameId,
				DefendRequest(
					wrongPlayer,
					Card("HEARTS", "SIX"),
					Card("HEARTS", "SEVEN")
				)
			)
		}
		
		assertEquals("Player $wrongPlayer is not the current defender", ex.message)
		verify(exactly = 1) { persistence.loadGame(gameId) }
		verify(exactly = 0) { persistence.saveGame(any(), any()) }
	}
	
	@Test
	fun `defend throws when persistence is missing`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			Controller(null).defend(
				"game-1",
				DefendRequest("p1", Card("HEARTS", "SIX"), Card("HEARTS", "SEVEN"))
			)
		}
		
		assertEquals("Persistence is not configured", ex.message)
	}
	
	@Test
	fun `defend throws when gameId is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.defend(
				null,
				DefendRequest("p1", Card("HEARTS", "SIX"), Card("HEARTS", "SEVEN"))
			)
		}
		
		assertEquals("gameId is required", ex.message)
	}
	
	@Test
	fun `defend throws when request is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.defend("game-1", null)
		}
		
		assertEquals("defendRequest is required", ex.message)
	}
	
	@Test
	fun `pass updates game saves it and returns ok`() {
		val gameId = "game-pass"
		val game = Game.create(listOf(PlayerId("p1"), PlayerId("p2")))
		every { persistence.loadGame(gameId) } returns game
		every { persistence.saveGame(gameId, game) } returns Unit
		
		val response = controller.pass(
			gameId,
			PassRequest("p1")
		)
		
		assertEquals(HttpStatus.OK, response.statusCode)
		assertNotNull(response.body)
		assertEquals(gameId, response.body!!.getGameId())
		verify(exactly = 1) { persistence.loadGame(gameId) }
		verify(exactly = 1) { persistence.saveGame(gameId, game) }
	}
	
	@Test
	fun `pass throws when persistence is missing`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			Controller(null).pass("game-1", PassRequest("p1"))
		}
		
		assertEquals("Persistence is not configured", ex.message)
	}
	
	@Test
	fun `pass throws when gameId is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.pass(null, PassRequest("p1"))
		}
		
		assertEquals("gameId is required", ex.message)
	}
	
	@Test
	fun `pass throws when request is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.pass("game-1", null)
		}
		
		assertEquals("passRequest is required", ex.message)
	}
	
	@Test
	fun `supply updates game saves it and returns ok`() {
		val gameId = "game-supply"
		val game = mockk<Game>()
		
		every { persistence.loadGame(gameId) } returns game
		every { game.supplyCard(any(), any()) } returns game
		every { persistence.saveGame(gameId, game) } returns Unit
		
		every { game.isGameOver() } returns false
		every { game.getDeckCards() } returns emptyList()
		every { game.getTrumpSuit() } returns Suit.HEARTS
		every { game.getAttacker() } returns PlayerId("p1")
		every { game.getDefender() } returns PlayerId("p2")
		every { game.getPlayers() } returns listOf(PlayerId("p1"), PlayerId("p2"))
		every { game.getPlayerHand(any()) } returns null
		every { game.getRoundCardPairings() } returns emptyMap()
		every { game.getSupplyPasses() } returns emptyList()
		
		val response = controller.supply(
			gameId,
			SupplyRequest("p1", Card("HEARTS", "SIX"))
		)
		
		assertEquals(HttpStatus.OK, response.statusCode)
		assertNotNull(response.body)
		assertEquals(gameId, response.body!!.getGameId())
		verify(exactly = 1) { persistence.loadGame(gameId) }
		verify(exactly = 1) { persistence.saveGame(gameId, game) }
	}
	
	@Test
	fun `supply throws when persistence is missing`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			Controller(null).supply("game-1", SupplyRequest("p1", Card("HEARTS", "SIX")))
		}
		
		assertEquals("Persistence is not configured", ex.message)
	}
	
	@Test
	fun `supply throws when gameId is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.supply(null, SupplyRequest("p1", Card("HEARTS", "SIX")))
		}
		
		assertEquals("gameId is required", ex.message)
	}
	
	@Test
	fun `supply throws when request is null`() {
		val ex = assertThrows(IllegalArgumentException::class.java) {
			controller.supply("game-1", null)
		}
		
		assertEquals("supplyRequest is required", ex.message)
	}
}