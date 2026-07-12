package hwr.oop.examples.template.cli

import hwr.oop.examples.template.core.Card
import hwr.oop.examples.template.core.Game
import hwr.oop.examples.template.core.Rank
import hwr.oop.examples.template.core.Suit

internal fun parseCard(value: String): Card {
	val parts = value.split(":", limit = 2)
	require(parts.size == 2) {
		"Card must be written as SUIT:RANK"
	}
	return Card(
		suit = Suit.valueOf(parts[0]),
		rank = Rank.valueOf(parts[1]),
	)
}

internal fun Game.printState() {
	println(getGameStatus())
}
