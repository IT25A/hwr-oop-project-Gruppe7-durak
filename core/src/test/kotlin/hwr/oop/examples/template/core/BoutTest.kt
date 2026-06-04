package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BoutTest {
	@Test
	fun `bout can be created`() {
		//given
		val attacker = PlayerHand.create(emptyList(), PlayerId("Attacker"))
		val defender = PlayerHand.create(emptyList(), PlayerId("Defender"))
		
		//when
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//then
		assertThat(bout).isNotNull()
	}
	
	@Test
	fun `attacker can play a card and it moves to attack stack`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(emptyList(), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val success = bout.attack(attackCard)
		
		//then
		assertThat(success).isTrue
		assertThat(bout.getAttackStack().cards()).containsExactly(attackCard)
	}
	
	@Test
	fun `return is false if card is not in attacker hand`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(emptyList(), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val card = Card(Suit.HEARTS, Rank.EIGHT)
		
		//then
		org.junit.jupiter.api.Assertions.assertThrows(AttackerDoesNotHaveCardException::class.java) { bout.attack(card) }
	}
	
	@Test
	fun `attacking card is not contained in attack stack, throw exception`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(emptyList(), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val card = Card(Suit.HEARTS, Rank.EIGHT)
		val cardNotInStack = Card(Suit.HEARTS, Rank.NINE)
		
		//then
		org.junit.jupiter.api.Assertions.assertThrows(AttackStackDoesNotContainCardException::class.java) {
			bout.defend(cardNotInStack, card)
		}
	}
	
	@Test
	fun `defending card is not contained in defending stack, throw DefenderDoesNotHaveCardException`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(emptyList(), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		//when
		val card = Card(Suit.HEARTS, Rank.EIGHT)
		bout.attack(attackCard)
		//then
		org.junit.jupiter.api.Assertions.assertThrows(DefenderDoesNotHaveCardException::class.java) {
			bout.defend(attackCard, card)
		}
	}
	
	@Test
	fun `defender can beat a card with higher rank same suit`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val defendCard = Card(Suit.SPADES, Rank.KING)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		bout.attack(attackCard)
		val success = bout.defend(attackCard, defendCard)
		
		//then
		assertThat(success).isTrue
		assertThat(bout.getDefendStack().cards()).containsExactly(defendCard)
	}
	
	
	
	@Test
	fun `defender can beat non-trump with trump`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.KING)
		val defendCard = Card(Suit.HEARTS, Rank.SIX) // HEARTS is trump
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		bout.attack(attackCard)
		val success = bout.defend(attackCard, defendCard)
		
		//then
		assertThat(success).isTrue
		assertThat(bout.isFullyDefended()).isTrue
	}
	
	@Test
	fun `defender cannot beat lower trump with non-trump`() {
		//given
		val attackCard = Card(Suit.HEARTS, Rank.KING) // trump that's high
		val defendCard = Card(Suit.SPADES, Rank.ACE) // non-trump even though high
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		bout.attack(attackCard)
		val success = bout.defend(attackCard, defendCard)
		
		//then
		assertThat(success).isFalse
	}
	
	@Test
	fun `fully defended bout returns defender win with table cards`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val defendCard = Card(Suit.SPADES, Rank.KING)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		bout.attack(attackCard)
		bout.defend(attackCard, defendCard)
		val result = bout.resolve()
		
		//then
		assertThat(result.defenderWon).isTrue
		assertThat(result.tableCards).containsExactlyInAnyOrder(attackCard, defendCard)
		assertThat(result.winner.getId()).isEqualTo(defender.getId())
		assertThat(bout.pairings()).isNotEmpty
		
	}
	
	@Test
	fun `resolve as attacker win clears pairings when there is at least one defended card`() {
		// given: attacker has two cards, defender has one card to defend the first attack
		val cardA1 = Card(Suit.SPADES, Rank.SIX)
		val cardA2 = Card(Suit.SPADES, Rank.SEVEN)
		val attackerCards = mutableListOf(cardA1, cardA2)
		val attacker = PlayerHand.create(attackerCards, PlayerId("Attacker"))
		
		val defendCard = Card(Suit.SPADES, Rank.KING)
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		// when: first attack is defended -> pairings gets filled
		assertThat(bout.attack(cardA1)).isTrue()
		assertThat(bout.defend(cardA1, defendCard)).isTrue()
		// ensure pairings is non-empty now
		assertThat(bout.pairings()).isNotEmpty()
		
		// then: attacker plays a second attack that is NOT defended
		assertThat(bout.attack(cardA2)).isTrue()
		
		// now resolve: not fully defended -> attacker wins -> resolve() calls reset()
		val result = bout.resolve()
		
		// expectations: attacker won, and reset() must have cleared pairings
		assertThat(result.defenderWon).isFalse
		assertThat(bout.pairings()).isEmpty()
		
		// defender should have taken all table/stack cards
		// (attackStack/defendStack reset by reset())
		assertThat(bout.getAttackStack().cards()).isEmpty()
		assertThat(bout.getDefendStack().cards()).isEmpty()
		
		// defender's hand should now contain the attacked cards + defended card
		val defenderCards = bout.defender.cards()
		assertThat(defenderCards).contains(cardA1, cardA2, defendCard)
	}
	
	@Test
	fun `undefended bout returns attacker win and defender takes cards`() {
		//given
		val attackCard = Card(Suit.SPADES, Rank.KING)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(listOf(Card(Suit.HEARTS, Rank.KING)), PlayerId("Defender")) // can't beat spades with non-trump hearts
		val bout = Bout(attacker, defender, Suit.CLUBS)
		
		//when
		bout.attack(attackCard)
		val result = bout.resolve()
		
		//then
		assertThat(result.defenderWon).isFalse
		assertThat(result.tableCards).isEmpty()
		assertThat(result.winner.getId()).isEqualTo(attacker.getId())
		
		// defender should now have the attack card
		assertThat(bout.defender.cards()).contains(attackCard)
		
		assertThat(bout.getAttackStack().cards().toList()).isEmpty()
		assertThat(bout.getDefendStack().cards().toList()).isEmpty()
		assertThat(bout.pairings()).isEmpty()
		
	}
	
	@Test
	fun `attacker win clears the table pile`() {
		//given
		val attackCard1 = Card(Suit.SPADES, Rank.SIX)
		val attackCard2 = Card(Suit.SPADES, Rank.KING)
		val attacker = PlayerHand.create(listOf(attackCard1, attackCard2), PlayerId("Attacker"))
		val defendCard = Card(Suit.SPADES, Rank.KING)
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when: defender wins first (tablePile gets filled)
		bout.attack(attackCard1)
		bout.defend(attackCard1, defendCard)
		bout.resolve()
		
		// tablePile is now filled
		assertThat(bout.tablePile()).isNotEmpty()
		
		// then: attacker wins (should clear tablePile)
		bout.promoteDefendToAttack()
		bout.attack(attackCard2)
		val result = bout.resolve()
		
		//then
		assertThat(result.defenderWon).isFalse
		assertThat(bout.tablePile()).isEmpty()
	}
	
	@Test
	fun `finalizeRound clears table pile and does not duplicate discard on repeated finalize`() {
		// given: einfache erfolgreiche Verteidigung
		val attackingCard = Card(Suit.HEARTS, Rank.SIX)
		val defendingCard = Card(Suit.HEARTS, Rank.SEVEN)
		
		val attacker = PlayerHand.create(listOf(attackingCard), PlayerId("attacker"))
		val defender = PlayerHand.create(listOf(defendingCard), PlayerId("defender"))
		val bout = Bout(attacker, defender, Suit.CLUBS)
		
		// when: Angriff und erfolgreiche Verteidigung
		assertThat(bout.attack(attackingCard)).isTrue()
		assertThat(bout.defend(attackingCard, defendingCard)).isTrue()
		
		// resolve -> tableCards should be non-empty (sichert Tisch-Karten)
		val result = bout.resolve()
		assertThat(result.tableCards).isNotEmpty
		
		// finalizeRound: toDiscard = tablePile(2) + attackStack(1) + defendStack(1) => 4 Karten
		val discard = DiscardPile()
		bout.finalizeRound(discard)
		
		// nach erstem finalize: discard enthält die erwarteten Karten
		assertThat(discard.cards()).isNotEmpty
		assertThat(discard.cards().size).isEqualTo(4)
		
		// Stacks wurden zurückgesetzt (über reset())
		assertThat(bout.getAttackStack().cards()).isEmpty()
		assertThat(bout.getDefendStack().cards()).isEmpty()
		
		// zweiter Aufruf: im Original unverändert (bleibt 4); wenn tablePile.clear() entfernt wurde -> würde es 6
		bout.finalizeRound(discard)
		assertThat(discard.cards().size).isEqualTo(4)
	}
	

	

	@Test
	fun `promote defended cards to attacks and finalize moves to discard`() {
		// given
		val attackCard = Card(Suit.SPADES, Rank.SIX)
		val defendCard = Card(Suit.SPADES, Rank.KING)
		val attacker = PlayerHand.create(listOf(attackCard), PlayerId("Attacker"))
		val defender = PlayerHand.create(listOf(defendCard), PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)

		// when: attacker plays and defender successfully defends
		bout.attack(attackCard)
		bout.defend(attackCard, defendCard)
		val result = bout.resolve()

		// defender won and table contains both cards
		assertThat(result.defenderWon).isTrue

		// now promote defender cards to become attacks
		bout.promoteDefendToAttack()
		// the defend card should now be present in attack stack as a new attack
		assertThat(bout.getAttackStack().cards()).contains(defendCard)

		// finalize the round: create discard pile and move all table cards there
		val discard = DiscardPile()
		bout.finalizeRound(discard)

		// after finalize, discard pile contains previous table cards and stacks are cleared
		assertThat(discard.cards()).contains(attackCard, defendCard)
		assertThat(bout.getAttackStack().cards()).isEmpty()
		assertThat(bout.getDefendStack().cards()).isEmpty()
	}
}