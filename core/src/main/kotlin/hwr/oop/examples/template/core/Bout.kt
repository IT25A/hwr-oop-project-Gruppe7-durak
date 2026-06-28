package hwr.oop.examples.template.core

import kotlinx.serialization.Serializable

@Serializable
class Bout(
	var attacker: PlayerHand,
	var defender: PlayerHand,
	private val trump: Suit,
) {
	private val attackStack = AttackStack()
	private val defendStack = CardDefend()
	
	private val pairings: MutableMap<Card, Card?> = mutableMapOf()
	private val tablePile: MutableList<Card> = mutableListOf()
	
	fun getAttackStack(): AttackStack = attackStack
	fun getDefendStack(): CardDefend = defendStack
	fun pairings(): Map<Card, Card?> = pairings
	fun tablePile(): List<Card> = tablePile
	fun attackStackCards(): List<Card> = attackStack.cards().toList()
	fun defendStackCards(): List<Card> = defendStack.cards().toList()
	
	
	fun attack(card: Card): Bout {
		if (!attacker.contains(card)) throw AttackerDoesNotHaveCardException("$card")
		attacker = attacker.without(card)
		attackStack.add(card)
		pairings[card] = null
		return this
	}
	
	fun addAttackFromOther(card: Card) {
		attackStack.add(card)
		pairings[card] = null
	}
	
	fun defend(attackingCard: Card, defendingCard: Card): Bout {
		if (!attackStack.cards()
				.contains(attackingCard)
		) throw AttackStackDoesNotContainCardException("$attackingCard")
		if (!defender.contains(defendingCard)) throw DefenderDoesNotHaveCardException("$attackingCard")
		if (pairings[attackingCard] != null) throw PairingCardWasAlreadyBeenDefendedException("$attackingCard")
		
		val defendingWins = cardBeats(attackingCard, defendingCard)
		if (defendingWins) {
			defender = defender.without(defendingCard)
			defendStack.add(defendingCard)
			pairings[attackingCard] = defendingCard
			return this
		}
		throw CardDoesNotBeatAttackingCardException("$attackingCard", "$defendingCard")
	}
	
	
	fun ranksOnTable(): Set<Rank> {
		val ranks = mutableSetOf<Rank>()
		ranks.addAll(pairings.keys.map { it.rank() })
		ranks.addAll(pairings.values.filterNotNull().map { it.rank() })
		return ranks
	}
	
	fun cardBeats(attacking: Card, defending: Card): Boolean {
		val attackRank = attacking.rank()
		val defendRank = defending.rank()
		val attackValue = attacking.getCardValue(attackRank)
		val defendValue = defending.getCardValue(defendRank)
		
		if (attacking.suit() == defending.suit()) {
			return defendValue > attackValue
		}
		
		if (defending.suit() == trump && attacking.suit() != trump) {
			return true
		}
		
		return false
	}
	
	fun isFullyDefended(): Boolean {
		return attackStack.cards().all { pairings[it] != null }
	}
	
	fun resolve(): BoutResult {
		if (isFullyDefended()) {
			
			val tableCards = (attackStack.cards() + defendStack.cards()).toList()
			tablePile.addAll(tableCards)
			
			return BoutResult(defenderWon = true, tableCards = tableCards, winner = defender)
		} else {
			
			val allToTake = mutableListOf<Card>()
			allToTake.addAll(tablePile)
			allToTake.addAll(attackStack.cards())
			allToTake.addAll(defendStack.cards())
			defender = defender.withAdded(allToTake)
			
			tablePile.clear()
			reset()
			return BoutResult(defenderWon = false, tableCards = emptyList(), winner = attacker)
		}
	}
	
	private fun reset() {
		attackStack.clear()
		defendStack.clear()
		pairings.clear()
	}
	
	fun promoteDefendToAttack() {
		
		val finishedAttacks = attackStack.cards()
		
		finishedAttacks.forEach { pairings.remove(it) }
		
		attackStack.clear()
		
		val promoted = defendStack.cards()
		promoted.forEach { attackStack.add(it) }
		
		promoted.forEach { pairings[it] = null }
		
		defendStack.clear()
		
	}
	
	fun finalizeRound(discard: DiscardPile) {
		val toDiscard = mutableListOf<Card>()
		
		toDiscard.addAll(tablePile)
		
		toDiscard.addAll(attackStack.cards().filter { card -> !toDiscard.contains(card) })
		toDiscard.addAll(defendStack.cards().filter { card -> !toDiscard.contains(card) })
		if (toDiscard.isNotEmpty()) discard.addAll(toDiscard)
		
		tablePile.clear()
		reset()
	}
}