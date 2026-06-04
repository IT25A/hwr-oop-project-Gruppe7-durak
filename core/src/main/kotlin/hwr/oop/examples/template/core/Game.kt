package hwr.oop.examples.template.core


/**
 * Represents a Baby Durak game with 2-4 players.
 *
 * Rules:
 * - Attacker plays cards, defender must beat all with same suit or trump
 * - Other players can join attacking (except first attacker and current defender)
 * - Round ends when defender wins (beats all) or loses (can't beat a card)
 * - Loser takes all cards; roles rotate accordingly
 */
class Game(
	private var handsOfPlayers: Map<PlayerId, PlayerHand>,
	private val players: List<PlayerId>,
	private var deck: MutableDeck,
	private val discard: DiscardPile = DiscardPile(),
	private val trump: Suit = Suit.HEARTS,
	private var currentAttackerIndex: Int = 0,
	private var currentDefenderIndex: Int = 1,
	private var roundActive: Boolean = false,
	private var roundCardPairings: MutableMap<Card, Card?> = mutableMapOf(), // attack -> defend (or null if undefended)
	private var currentRoundAttackers: MutableList<PlayerId> = mutableListOf()
) {
	companion object {
		fun create(playerIds: List<PlayerId>): Game {
			val playercount = playerIds.size
			if (playercount !in 2..4) {
				throw InvalidPlayerNumberException("Player count must be between 2 and 4, but was $playercount")
			}
			
			val deckMutable = Deck.createRandomDeck().toMutableDeck()
			val handsOfPlayers = mutableMapOf<PlayerId, PlayerHand>()
			
			// Deal 6 cards to each player
			for (playerId in playerIds) {
				var playerHand = PlayerHand.create(id = playerId)
				playerHand = deckMutable.dealTo(playerHand, 6)
				handsOfPlayers[playerId] = playerHand
			}
			
			return Game(
				handsOfPlayers = handsOfPlayers,
				players = playerIds,
				deck = deckMutable,
				currentAttackerIndex = 0,
				currentDefenderIndex = 1,
				currentRoundAttackers = mutableListOf(playerIds[0])
			)
		}
	}
	
	fun getAttacker(): PlayerId = players[currentAttackerIndex]
	
	fun getDefender(): PlayerId = players[currentDefenderIndex]
	
	fun getPlayerHand(playerId: PlayerId): PlayerHand? = handsOfPlayers[playerId]
	
	fun isRoundActive(): Boolean = roundActive
	
	fun getCurrentRoundAttackers(): List<PlayerId> = currentRoundAttackers.toList()
	
	fun getRoundCardPairings(): Map<Card, Card?> = roundCardPairings.toMap()
	
	/**
	 * Start a new round with the current attacker vs defender
	 */
	fun startRound() {
		if (roundActive) {
			throw IllegalStateException("Round already active")
		}
		
		roundCardPairings.clear()
		currentRoundAttackers.clear()
		currentRoundAttackers.add(getAttacker())
		roundActive = true
	}
	
	/**
	 * Primary attacker plays a card
	 */
	fun attackWithCard(card: Card): Boolean {
		if (!roundActive) {
			throw IllegalStateException("No active round")
		}
		
		val attacker = getAttacker()
		val attackerHand = handsOfPlayers[attacker] ?: throw IllegalStateException("Attacker not found")
		
		if (!attackerHand.contains(card)) {
			throw IllegalStateException("Attacker does not have the card: $card")
		}
		
		// Can only attack with cards that match rank of already-played cards or new ranks
		if (roundCardPairings.isNotEmpty()) {
			val ranksOnTable = roundCardPairings.keys.map { it.rank() }.toSet()
			if (card.rank() !in ranksOnTable && roundCardPairings.size >= handsOfPlayers[getDefender()]?.cards()?.size ?: 0) {
				return false
		}
		}
		
		// Add the attacking card with no defense initially
		roundCardPairings[card] = null
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[attacker] = attackerHand.without(card)
		}
		
		return true
	}
	
	/**
	 * Other players can join the attack (only if not attacker or defender)
	 */
	fun joinAttack(playerId: PlayerId, card: Card): Boolean {
		if (!roundActive) {
			throw IllegalStateException("No active round")
		}
		
		if (playerId == getAttacker() || playerId == getDefender()) {
			return false // Attacker and defender cannot join
		}
		
		if (currentRoundAttackers.contains(playerId)) {
			return false // Already attacking
		}
		
		val joiningHand = handsOfPlayers[playerId] ?: throw IllegalStateException("Player not found")
		
		if (!joiningHand.contains(card)) {
			return false // Player doesn't have the card
		}
		
		// Check if card rank matches ranks on table
		val ranksOnTable = roundCardPairings.keys.map { it.rank() }.toSet()
		if (card.rank() !in ranksOnTable) {
			return false
		}
		
		// Cannot exceed defender's maximum playable cards
		val defenderCardCount = handsOfPlayers[getDefender()]?.cards()?.size ?: 0
		if (roundCardPairings.size >= defenderCardCount) {
			return false
		}
		
		// Add the attacking card
		roundCardPairings[card] = null
		currentRoundAttackers.add(playerId)
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[playerId] = joiningHand.without(card)
		}
		
		return true
	}
	
	/**
	 * Defender beats a card
	 */
	fun defendCard(attackingCard: Card, defendingCard: Card): Boolean {
		if (!roundActive) {
			throw IllegalStateException("No active round")
		}
		
		if (!roundCardPairings.containsKey(attackingCard)) {
			throw IllegalStateException("Attacking card not in round")
		}
		
		val defender = getDefender()
		val defenderHand = handsOfPlayers[defender] ?: throw IllegalStateException("Defender not found")
		
		if (!defenderHand.contains(defendingCard)) {
			throw IllegalStateException("Defender does not have the card")
		}
		
		// Check if defending card beats attacking card
		val defendingWins = cardBeats(attackingCard, defendingCard)
		if (!defendingWins) {
			return false
		}
		
		// Record the pairing
		roundCardPairings[attackingCard] = defendingCard
		handsOfPlayers = handsOfPlayers.toMutableMap().apply {
			this[defender] = defenderHand.without(defendingCard)
		}
		
		return true
	}
	
	/**
	 * Check if defending card beats attacking card
	 */
	private fun cardBeats(attacking: Card, defending: Card): Boolean {
		val attackRank = attacking.rank()
		val defendRank = defending.rank()
		val attackValue = attacking.getCardValue(attackRank)
		val defendValue = defending.getCardValue(defendRank)
		
		// Same suit -> higher rank wins
		if (attacking.suit() == defending.suit()) {
			return defendValue > attackValue
		}
		
		// Trump beats non-trump
		if (defending.suit() == trump && attacking.suit() != trump) {
			return true
		}
		
		return false
	}
	
	/**
	 * Check if the defender has beaten all attacking cards
	 */
	fun isRoundFullyDefended(): Boolean {
		return roundCardPairings.values.none { it == null }
	}
	
	/**
	 * Check if there are undefended cards
	 */
	fun hasUndefendedCards(): Boolean {
		return roundCardPairings.values.any { it == null }
	}
	
	/**
	 * End the round - determine winner and apply consequences
	 */
	fun endRound() {
		if (!roundActive) {
			throw IllegalStateException("No active round")
		}
		
		val defender = getDefender()
		val defenderWon = isRoundFullyDefended()
		
		if (defenderWon) {
			// Defender won: put all cards in discard, defender becomes next attacker
			val allCards = mutableListOf<Card>()
			roundCardPairings.forEach { (attack, defend) ->
				allCards.add(attack)
				if (defend != null) {
					allCards.add(defend)
				}
			}
			discard.addAll(allCards)
			
			// Rotate: defender -> attacker, current attacker -> defender
			val nextAttackerIndex = currentDefenderIndex
			val nextDefenderIndex = (nextAttackerIndex + 1) % players.size
			
			currentAttackerIndex = nextAttackerIndex
			currentDefenderIndex = nextDefenderIndex
		} else {
			// Defender lost: defender takes all cards, attacker stays
			val allCards = mutableListOf<Card>()
			roundCardPairings.forEach { (attack, defend) ->
				allCards.add(attack)
				if (defend != null) {
					allCards.add(defend)
				}
			}
			
			handsOfPlayers = handsOfPlayers.toMutableMap().apply {
				this[defender] = (this[defender] ?: PlayerHand.create(id = defender)).withAdded(allCards)
			}
			
			// Next defender
			currentDefenderIndex = (currentDefenderIndex + 1) % players.size
		}
		
		roundActive = false
		roundCardPairings.clear()
		currentRoundAttackers.clear()
		currentRoundAttackers.add(players[currentAttackerIndex])
		
		// Deal new cards
		replenishHands()
	}
	
	/**
	 * Replenish player hands up to 6 cards
	 */
	fun replenishHands() {
		val updatedHands = handsOfPlayers.toMutableMap()
		
		// Attackers draw first (in order of joining)
		for (attackerId in currentRoundAttackers) {
			val hand = updatedHands[attackerId] ?: continue
			val needCards = 6 - hand.cards().size
			if (needCards > 0) {
				updatedHands[attackerId] = deck.dealTo(hand, needCards)
			}
		}
		
		// Then defender draws
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
	
	/**
	 * Check if game is over (only one player has cards)
	 */
	fun isGameOver(): Boolean {
		val playersWithCards = handsOfPlayers.count { (_, hand) -> hand.cards().isNotEmpty() }
		return playersWithCards <= 1
	}
	
	/**
	 * Get the loser (player with no cards at end of game)
	 */
	fun getLoser(): PlayerId? {
		if (!isGameOver()) return null
		return players.firstOrNull { handsOfPlayers[it]?.cards()?.isEmpty() == true }
	}
	
	fun isDeckEmpty(): Boolean = deck.cards.isEmpty()
	
	fun getGameStatus(): String {
		return """
			|=== DURAK GAME STATUS ===
			|Current Attacker: ${getAttacker()}
			|Current Defender: ${getDefender()}
			|Round Active: $roundActive
			|Cards on Table: ${roundCardPairings.size}
			|(Defended: ${roundCardPairings.count { it.value != null }}, Undefended: ${roundCardPairings.count { it.value == null }})
			|Deck remaining: ${deck.cards.size}
			|Discard pile: ${discard.cards().size}
			|Players:
			|${players.joinToString("\n") { playerId ->
				val hand = handsOfPlayers[playerId]
				val cardCount = hand?.cards()?.size ?: 0
				val roleStr = when {
					playerId == getAttacker() -> " (ATTACKER)"
					playerId == getDefender() -> " (DEFENDER)"
					currentRoundAttackers.contains(playerId) && playerId != getAttacker() -> " (ATTACKING)"
					else -> ""
				}
				"  $playerId: $cardCount cards$roleStr"
			}}
		""".trimMargin()
	}
}