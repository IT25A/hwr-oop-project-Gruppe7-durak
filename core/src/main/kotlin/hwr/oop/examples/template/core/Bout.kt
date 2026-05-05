package hwr.oop.examples.template.core

class Bout {
	private var firstCard: Card? = null
	private var secondCard: Card? = null
	
	fun attack(firstCard: Card, secondCard: Card): Card {
		this.firstCard = firstCard
		this.secondCard = secondCard
		if (firstCard.getCardValue(firstCard.rank()) > secondCard.getCardValue(secondCard.rank()) ) {
			return firstCard
		}
		else if (firstCard.getCardValue(firstCard.rank()) < secondCard.getCardValue(secondCard.rank())) {
			return secondCard
		}
		else (firstCard.getCardValue(firstCard.rank())== secondCard.getCardValue(firstCard.rank()))
			throw Exception("Trump is not implemented yet")
	}
	
}