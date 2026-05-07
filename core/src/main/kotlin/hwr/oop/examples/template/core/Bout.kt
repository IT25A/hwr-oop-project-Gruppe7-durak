package hwr.oop.examples.template.core

class Bout {
	companion object {
		private var firstCard: Card? = null
		private var secondCard: Card? = null

		fun compareCards(firstCard: Card, secondCard: Card): Card {
			this.firstCard = firstCard
			this.secondCard = secondCard
			if (firstCard.getCardValue(firstCard.rank()) > secondCard.getCardValue(secondCard.rank())) {
				return firstCard
			} else if (firstCard.getCardValue(firstCard.rank()) < secondCard.getCardValue(secondCard.rank())) {
				return secondCard
			} else {
				throw Exception("Trump is not implemented yet")
			}
		}
	}
}