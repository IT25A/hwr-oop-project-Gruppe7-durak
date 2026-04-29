package hwr.oop.examples.template.core

data class MutableDeck(
	val cards: MutableList<Card> = mutableListOf()
) {
	// z.B. Karte ziehen, hinzufügen, etc.
}
