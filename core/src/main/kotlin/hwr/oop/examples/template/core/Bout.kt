package hwr.oop.examples.template.core

class Bout(
	var attacker: PlayerHand,
	var defender: PlayerHand,
	private val trump: Suit,
) {
	private val attackStack = AttackStack()
	private val defendStack = CardDefend()
	
	// Map um zu tracken welche Angriffskarte von welcher Verteidigungskarte geschlagen wird
	private val pairings: MutableMap<Card, Card> = mutableMapOf()
	private val tablePile: MutableList<Card> = mutableListOf() // gesicherte Tisch-Karte
	
	fun getAttackStack(): AttackStack = attackStack
	fun getDefendStack(): CardDefend = defendStack
	fun pairings(): Map<Card, Card> = pairings
	fun tablePile(): List<Card> = tablePile
	
	// Angreifer spielt eine Karte
	fun attack(card: Card): Boolean {
		if (!attacker.contains(card)) throw AttackerDoesNotHaveCardException("Attacker does not have the card: $card")
		attacker = attacker.without(card)
		attackStack.add(card)
		return true
	}
	
	fun defend(attackingCard: Card, defendingCard: Card): Boolean {
		if (!attackStack.cards()
				.contains(attackingCard)
		) throw AttackStackDoesNotContainCardException("Attack stack does not contain the attacking card: $attackingCard")
		if (!defender.contains(defendingCard)) throw DefenderDoesNotHaveCardException("Defender does not have the card: $defendingCard")
		if (pairings.containsKey(attackingCard)) throw PairingCardWasAlreadyBeenDefendedException("The attacking card has already been defended: $attackingCard")
		
		val defendingWins = cardBeats(attackingCard, defendingCard)
		if (defendingWins) {
			defender = defender.without(defendingCard)
			defendStack.add(defendingCard)
			pairings[attackingCard] = defendingCard
			return true
		}
		return false
	}
	
	// Prüft ob defendingCard die attackingCard schlägt
	private fun cardBeats(attacking: Card, defending: Card): Boolean {
		val attackRank = attacking.rank()
		val defendRank = defending.rank()
		val attackValue = attacking.getCardValue(attackRank)
		val defendValue = defending.getCardValue(defendRank)
		
		// Gleiche Farbe -> höhere Karte gewinnt
		if (attacking.suit() == defending.suit()) {
			return defendValue > attackValue
		}
		
		// Verteidigungs-Karte ist Trump und Angriffs-Karte nicht -> Verteidiger gewinnt
		if (defending.suit() == trump && attacking.suit() != trump) {
			return true
		}
		
		// Ansonsten gewinnt Verteidiger nicht
		return false
	}
	
	// Prüft ob alle Angriffskarten verteidigt wurden
	fun isFullyDefended(): Boolean {
		return attackStack.cards().all { pairings.containsKey(it) }
	}
	
	// Auflösen der Runde
	fun resolve(): BoutResult {
		if (isFullyDefended()) {
			// Verteidiger hat gewonnen
			// WICHTIG: nicht sofort leeren! Caller kann noch promoteDefendToAttack() aufrufen.
			// Sichere die Tischkarten in tablePile, aber lasse attackStack/defendStack vorhanden.
			val tableCards = (attackStack.cards() + defendStack.cards()).toList()
			tablePile.addAll(tableCards)
			// Stacks nicht leeren — Caller macht das nach promote/finalize
			return BoutResult(defenderWon = true, tableCards = tableCards, winner = defender)
		} else {
			// Angreifer hat gewonnen: Verteidiger nimmt ALLE Tisch-Karten
			val allToTake = mutableListOf<Card>()
			allToTake.addAll(tablePile)
			allToTake.addAll(attackStack.cards())
			allToTake.addAll(defendStack.cards())
			defender = defender.withAdded(allToTake)
			// alles zurücksetzen
			tablePile.clear()
			reset()
			return BoutResult(defenderWon = false, tableCards = emptyList(), winner = attacker)
		}
	}
	
	// Hilfsmethode um Stacks zurückzusetzen
	private fun reset() {
		attackStack.clear()
		defendStack.clear()
		pairings.clear()
	}
	
	// Promoviere alle Verteidigungs-Karten zu neuen Angriffen (sie werden auf den Attack-Stack verschoben)
	fun promoteDefendToAttack() {
		// 1) sichere die aktuellen Angriffs-Karten (alte Angriffe) in tablePile
		val finishedAttacks = attackStack.cards()
		
		// 2) entferne die alten Angriffe aus pairings (sie gelten nicht mehr)
		finishedAttacks.forEach { pairings.remove(it) }
		
		// 3) nun clear attackStack (alte Angriffe sind jetzt gesichert)
		attackStack.clear()
		
		// 4) die Verteidigungs-Karten werden zu neuen aktiven Angriffen
		val promoted = defendStack.cards()
		promoted.forEach { attackStack.add(it) }
		
		// 5) verteidigungs-stack kann nun geleert werden (sie sind jetzt aktive Angriffe)
		defendStack.clear()
	}
	
	// Finalisiere die Runde: verschiebe alle Tischkarten in den Ablagestapel und leere Stacks
	fun finalizeRound(discard: DiscardPile) {
		val toDiscard = mutableListOf<Card>()
		toDiscard.addAll(tablePile)
		toDiscard.addAll(attackStack.cards())
		toDiscard.addAll(defendStack.cards())
		if (toDiscard.isNotEmpty()) discard.addAll(toDiscard)
		// alles leeren
		tablePile.clear()
		reset()
	}
}

// Datenklasse für das Ergebnis
data class BoutResult(
	val defenderWon: Boolean,  // true wenn Verteidiger gewonnen, false wenn Angreifer
	val tableCards: List<Card>,  // Karten die auf dem Tisch liegen (nur wenn Verteidiger gewonnen)
	val winner: PlayerHand,  // der Gewinner dieser Runde
)