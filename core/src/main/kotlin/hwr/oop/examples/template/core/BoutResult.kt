package hwr.oop.examples.template.core

data class BoutResult(
	val defenderWon: Boolean,
	val tableCards: List<Card>,
	val winner: PlayerHand,
) {}

