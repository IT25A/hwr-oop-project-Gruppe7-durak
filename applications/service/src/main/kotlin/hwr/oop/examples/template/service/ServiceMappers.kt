package hwr.oop.examples.template.service

import hwr.oop.examples.template.core.Card as CoreCard
import hwr.oop.examples.template.core.Game as CoreGame
import hwr.oop.examples.template.core.PlayerHand as CorePlayerHand
import hwr.oop.examples.template.core.PlayerId
import hwr.oop.examples.template.core.Rank
import hwr.oop.examples.template.core.Suit
import hwr.oop.examples.template.service.model.AttackStack
import hwr.oop.examples.template.service.model.Card
import hwr.oop.examples.template.service.model.GameState
import hwr.oop.examples.template.service.model.GameStatus
import hwr.oop.examples.template.service.model.PlayerHand

internal fun Card.toCoreCard(): CoreCard {
	return CoreCard(
		Suit.valueOf(requireNotNull(suit) { "Card suit is required" }),
		Rank.valueOf(requireNotNull(rank) { "Card rank is required" }),
	)
}

internal fun CoreCard.toApiCard(): Card {
	return Card(
		suit().name,
		rank().name,
	)
}

internal fun CorePlayerHand.toApiPlayerHand(): PlayerHand {
	return PlayerHand(
		getId().asString(),
		cards().map { it.toApiCard() },
	)
}

internal fun CoreGame.toApiGameState(gameId: String): GameState {
	return GameState(
		gameId,
		if (isGameOver()) GameStatus.FINISHED else GameStatus.IN_PROGRESS,
		getDeckCards().map { it.toApiCard() },
		getTrumpSuit().name,
		getAttacker().asString(),
		getDefender().asString(),
		getPlayers().mapNotNull { getPlayerHand(it)?.toApiPlayerHand() },
		getRoundCardPairings().entries.map { (attackCard, defenseCard) ->
			AttackStack(attackCard.toApiCard()).apply {
				defenseCard?.let { setDefenseCard(it.toApiCard()) }
			}
		},
		getSupplyPasses().map { it.asString() },
	)
}

internal fun String.toPlayerId(): PlayerId = PlayerId(this)
