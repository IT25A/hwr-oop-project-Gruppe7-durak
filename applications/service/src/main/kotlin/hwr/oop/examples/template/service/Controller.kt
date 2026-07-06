package hwr.oop.examples.template.service

import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.GamePersistence
import hwr.oop.examples.template.core.PlayerId
import hwr.oop.examples.template.service.api.GameActionApi
import hwr.oop.examples.template.service.api.GameApi
import hwr.oop.examples.template.service.model.AttackRequest
import hwr.oop.examples.template.service.model.DefendRequest
import hwr.oop.examples.template.service.model.GameCreatedResponse
import hwr.oop.examples.template.service.model.GameState
import hwr.oop.examples.template.service.model.PassRequest
import hwr.oop.examples.template.service.model.StartGameRequest
import hwr.oop.examples.template.service.model.SupplyRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class Controller(
	private val persistence: GamePersistence? = null,
) : GameApi, GameActionApi {

	override fun getGame(gameId: String?): ResponseEntity<GameState> {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val id = requireNotNull(gameId) { "gameId is required" }
		return ResponseEntity.ok(gamePersistence.loadGame(id).toApiGameState(id))
	}

	override fun startGame(startGameRequest: @Valid StartGameRequest?): ResponseEntity<GameCreatedResponse> {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val request = requireNotNull(startGameRequest) { "startGameRequest is required" }
		val game = Game.create(request.playerIds.map { PlayerId(it) })
		val gameId = UUID.randomUUID().toString()
		gamePersistence.saveGame(gameId, game)
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(GameCreatedResponse(gameId))
	}

	override fun attack(
		gameId: String?,
		attackRequest: @Valid AttackRequest?,
	): ResponseEntity<GameState> {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val id = requireNotNull(gameId) { "gameId is required" }
		val request = requireNotNull(attackRequest) { "attackRequest is required" }
		val game = gamePersistence.loadGame(id)
		game.supplyCard(PlayerId(request.playerId), request.card.toCoreCard())
		gamePersistence.saveGame(id, game)
		return ResponseEntity.ok(game.toApiGameState(id))
	}

	override fun defend(
		gameId: String?,
		defendRequest: @Valid DefendRequest?,
	): ResponseEntity<GameState> {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val id = requireNotNull(gameId) { "gameId is required" }
		val request = requireNotNull(defendRequest) { "defendRequest is required" }
		val game = gamePersistence.loadGame(id)
		require(request.playerId == game.getDefender().asString()) {
			"Player ${request.playerId} is not the current defender"
		}
		game.defendCard(
			request.attackCard.toCoreCard(),
			request.defenseCard.toCoreCard(),
		)
		gamePersistence.saveGame(id, game)
		return ResponseEntity.ok(game.toApiGameState(id))
	}

	override fun pass(
		gameId: String?,
		passRequest: @Valid PassRequest?,
	): ResponseEntity<GameState> {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val id = requireNotNull(gameId) { "gameId is required" }
		val request = requireNotNull(passRequest) { "passRequest is required" }
		val game = gamePersistence.loadGame(id)
		game.passSupply(PlayerId(request.playerId))
		gamePersistence.saveGame(id, game)
		return ResponseEntity.ok(game.toApiGameState(id))
	}

	override fun supply(
		gameId: String?,
		supplyRequest: @Valid SupplyRequest?,
	): ResponseEntity<GameState> {
		val gamePersistence = requireNotNull(persistence) { "Persistence is not configured" }
		val id = requireNotNull(gameId) { "gameId is required" }
		val request = requireNotNull(supplyRequest) { "supplyRequest is required" }
		val game = gamePersistence.loadGame(id)
		game.supplyCard(PlayerId(request.playerId), request.card.toCoreCard())
		gamePersistence.saveGame(id, game)
		return ResponseEntity.ok(game.toApiGameState(id))
	}
}
