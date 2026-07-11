package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable
class Game(
	private var handsOfPlayers: Map<PlayerId, PlayerHand>,
	private val players: List<PlayerId>,
	private var deck: MutableDeck,
	private val discard: DiscardPile = DiscardPile(),
	private val trump: Trump = Trump.of(Suit.HEARTS),
	private var currentAttackerIndex: Int = 0,
	private var currentDefenderIndex: Int = 1,
	private var roundActive: Boolean = false,
	private val roundCardPairings: MutableMap<Card, Card?> = mutableMapOf(),
	private val supplyPasses: MutableSet<PlayerId> = mutableSetOf(),
	private var currentRoundAttackers: MutableList<PlayerId> = mutableListOf(),
	private var currentBout: Bout? = null,
) {
	init {
		if (currentBout == null && roundActive) {
			initRound()
		}
	}

	companion object {
		fun create(playerIds: List<PlayerId>): Game {
			val playercount = playerIds.size
			if (playercount !in 2..6) {
				throw InvalidPlayerNumberException("$playercount")
			}
			
			val deckMutable = Deck.createRandomDeck().toMutableDeck()
			val (_, trump) = Trump.drawFromDeck(deckMutable)
			
			val handsOfPlayers = mutableMapOf<PlayerId, PlayerHand>()
			
			
			for (playerId in playerIds) {
				var playerHand = PlayerHand.create(id = playerId)
				playerHand = deckMutable.dealTo(playerHand, 6)
				handsOfPlayers[playerId] = playerHand
			}
			
 		val game = Game(
				handsOfPlayers = handsOfPlayers,
				players = playerIds,
				deck = deckMutable,
			  trump = trump,
				currentAttackerIndex = 0,
				currentDefenderIndex = 1,
				currentRoundAttackers = mutableListOf(playerIds[0]),
				roundActive = true
			)
			return game
		}
	}
	
	private fun initRound() {
		roundCardPairings.clear()
		supplyPasses.clear()
		currentRoundAttackers.clear()
		currentRoundAttackers.add(getAttacker())

		val attackerHand = handsOfPlayers[getAttacker()] ?: PlayerHand.create(id = getAttacker())
		val defenderHand = handsOfPlayers[getDefender()] ?: PlayerHand.create(id = getDefender())
		currentBout = Bout(attackerHand, defenderHand, trump.suit())

		roundActive = true
	}
	
	fun getAttacker(): PlayerId = players[currentAttackerIndex]
	
	fun getDefender(): PlayerId = players[currentDefenderIndex]

	fun getPlayers(): List<PlayerId> = players.toList()
	
	fun getPlayerHand(playerId: PlayerId): PlayerHand? = handsOfPlayers[playerId]
	
	fun isRoundActive(): Boolean = roundActive
	
	fun getCurrentRoundAttackers(): List<PlayerId> = currentRoundAttackers.toList()
	
	fun getRoundCardPairings(): Map<Card, Card?> = roundCardPairings.toMap()

	fun getSupplyPasses(): List<PlayerId> = supplyPasses.toList()

	fun getDeckCards(): List<Card> = deck.cards.toList()

	fun getTrumpSuit(): Suit = trump.suit()
	
	fun attackWithCard(card: Card): Game {
		if (!roundActive) {
			throw NoActiveRoundException()
		}
		
		val attacker = getAttacker()
		val attackerHand = handsOfPlayers[attacker] ?: throw AttackerNotFoundException()
		val bout = currentBout ?: throw NoActiveBoutException()
		
		if (!attackerHand.contains(card)) {
			throw AttackerDoesNotHaveCardException("$card")
		}
		
		val ranksOnTable = bout.ranksOnTable()
		val isFirstAttack = bout.attackStackCards().isEmpty()
		
		if (!isFirstAttack && card.rank() !in ranksOnTable && bout.attackStackCards().size >= handsOfPlayers[getDefender()]?.cards()?.size ?: 0) {
			throw RankNotOnTableException()
		}
		
		bout.attack(card)
		
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[attacker] = bout.attacker
		}
		
		roundCardPairings[card] = null
		supplyPasses.clear()
		
		return this
	}
	
	
	fun joinAttack(playerId: PlayerId, card: Card): Game {
		if (!roundActive) {
			throw NoActiveRoundException()
		}
		
		if (playerId == getAttacker() || playerId == getDefender()) {
			throw AttackerAndDefenderCanNotJoinAttackException()
		}
		
		if (currentRoundAttackers.contains(playerId)) {
			throw AttackerCanNotJoinHisAttackException()
		}
		
		val joiningHand = handsOfPlayers[playerId] ?: throw JoinerNotFoundException()
		
		if (!joiningHand.contains(card)) {
			throw JoinerDoesNotHaveCardException()
		}
		
		val bout = currentBout ?: throw NoActiveBoutException()
		
		val ranksOnTable = bout.ranksOnTable()
		if (card.rank() !in ranksOnTable) {
			throw RankNotOnTableException()
		}
		
		val defenderCardCount = handsOfPlayers[getDefender()]?.cards()?.size ?: 0
		if (bout.attackStackCards().size > defenderCardCount) {
			throw DefenderDoesNotHaveEnoughCardsException()
		}
		
		
		bout.addAttackFromOther(card)
		currentRoundAttackers.add(playerId)
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[playerId] = joiningHand.without(card)
		}
		
		
		roundCardPairings[card] = null
		supplyPasses.clear()
		
		return this
	}
	
	
	fun defendCard(attackingCard: Card, defendingCard: Card): Game {
		if (!roundActive) {
			throw NoActiveRoundException()
		}
		
		val bout = currentBout ?: throw NoActiveBoutException()
		
		if (!bout.attackStackCards().contains(attackingCard)) {
			throw AttackStackDoesNotContainCardException("$attackingCard")
		}
		
		val defender = getDefender()
		val defenderHand = handsOfPlayers[defender] ?: throw DefenderNotFoundException()
		
		if (!defenderHand.contains(defendingCard)) {
			throw DefenderDoesNotHaveCardException("$defendingCard")
		}
		
		bout.defend(attackingCard, defendingCard)
		
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[defender] = bout.defender
		}
		
		roundCardPairings[attackingCard] = defendingCard
		
		return this
	}

	fun supplyCard(playerId: PlayerId, card: Card): Game {
		if (playerId == getAttacker()) {
			return attackWithCard(card)
		}
		return joinAttack(playerId, card)
	}

	fun passSupply(playerId: PlayerId): Game {
		if (!roundActive) {
			throw NoActiveRoundException()
		}
		
		if (playerId == getDefender()) {
			throw DefenderCanNotPassException()
		}
		
		if (!players.contains(playerId)) {
			throw JoinerNotFoundException()
		}
		
		if (!supplyPasses.add(playerId)) {
			throw PlayerAlreadyPassedSupplyException(playerId.asString())
		}
		
		val nonDefendingPlayers = players.filterNot { it == getDefender() }
		if (nonDefendingPlayers.all { it in supplyPasses }) {
			endRound()
		}
		
		return this
	}
	
	
	fun isRoundFullyDefended(): Boolean {
		val bout = currentBout ?: return true
		return bout.isFullyDefended()
	}
	
	
	fun hasUndefendedCards(): Boolean {
		val bout = currentBout ?: return false
		return bout.attackStackCards().any { bout.pairings()[it] == null }
	}
	
	
	fun endRound() {
		if (!roundActive) {
			throw NoActiveRoundException()
		}
		
		val bout = currentBout ?: throw NoActiveBoutException()
		val defender = getDefender()
		val result = bout.resolve()
		
		
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[getAttacker()] = bout.attacker
			this[defender] = bout.defender
		}
		
		if (result.defenderWon) {
			
			bout.finalizeRound(discard)
			
			val nextAttackerIndex = currentDefenderIndex
			val nextDefenderIndex = (nextAttackerIndex + 1) % players.size
			
			currentAttackerIndex = nextAttackerIndex
			currentDefenderIndex = nextDefenderIndex
		} else {
			
			currentDefenderIndex = (currentDefenderIndex + 1) % players.size
			currentAttackerIndex = (currentAttackerIndex + 1) % players.size
		}
		replenishHands()
		
		initRound()
	}
	
	
	fun replenishHands() {
		val updatedHands = handsOfPlayers.toMutableMap()
		
		
		for (attackerId in currentRoundAttackers) {
			val hand = updatedHands[attackerId] ?: continue
			val needCards = 6 - hand.cards().size
			if (needCards > 0) {
				updatedHands[attackerId] = deck.dealTo(hand, needCards)
			}
		}
		
		val defender = getDefender()
		val defenderHand = updatedHands[defender]
		if (defenderHand != null) {
			val needCards = 6 - defenderHand.cards().size
			if (needCards > 0) {
				updatedHands[defender] = deck.dealTo(defenderHand, needCards)
			}
		}
		
		handsOfPlayers = updatedHands
	}
	
	
	fun isGameOver(): Boolean {
		val playersWithCards = handsOfPlayers.count { (_, hand) -> hand.cards().isNotEmpty() }
		return playersWithCards <= 1
	}
	
	
	fun getLoser(): PlayerId? {
		if (!isGameOver()) return null
		return players.firstOrNull { handsOfPlayers[it]?.cards()?.isEmpty() == true }
	}
	
	fun isDeckEmpty(): Boolean = deck.cards.isEmpty()
	
	fun getGameStatus(): String {
		return """
			|=== DURAK GAME STATUS ===
			|Trump Suit: ${trump.suit()}
			|Current Attacker: ${getAttacker()}
			|Current Defender: ${getDefender()}
			|Round Active: $roundActive
			|Cards on Table: ${roundCardPairings.size}
			|(Defended: ${roundCardPairings.count { it.value != null }}, Undefended: ${roundCardPairings.count { it.value == null }})
			|Deck remaining: ${deck.cards.size}
			|Discard pile: ${discard.cards().size}
			|Players:
			|${
			players.joinToString("\n") { playerId ->
				val hand = handsOfPlayers[playerId]
				val cardCount = hand?.cards()?.size ?: 0
				val roleStr = when {
					playerId == getAttacker() -> " (ATTACKER)"
					playerId == getDefender() -> " (DEFENDER)"
					currentRoundAttackers.contains(playerId) && playerId != getAttacker() -> " (ATTACKING)"
					else -> ""
				}
				"  $playerId: $cardCount cards$roleStr"
			}
		}
		""".trimMargin()
	}
}
