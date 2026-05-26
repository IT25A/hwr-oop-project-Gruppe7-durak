package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BoutTest {
	@Test
	fun `bout can be created`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		val defender = PlayerHand(PlayerId("Defender"))
		
		//when
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//then
		assertThat(bout).isNotNull()
	}
	
	@Test
	fun `attacker can play a card and it moves to attack stack`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val card = attacker.cards.first()
		val success = bout.attack(card)
		
		//then
		assertThat(success).isTrue
		assertThat(attacker.cards).doesNotContain(card)
		assertThat(bout.getAttackStack().cards()).containsExactly(card)
	}
	
	@Test
	fun `return is false if card is not in attacker hand`() {
		//given
		
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val card = Card(Suit.HEARTS, Rank.EIGHT)
		val success = bout.attack(card)
		
		//then
		assertThat(success).isFalse
	}
	
	@Test
	fun `attacking card is not contained in attack stack, return false`() {
		//given
		
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val card = Card(Suit.HEARTS, Rank.EIGHT)
		val success = bout.defend(card, Card(Suit.HEARTS, Rank.NINE))
		
		//then
		assertThat(success).isFalse
	}
	
	@Test
	fun `defending card is not contained in defending stack, throw DefendingCardException`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		//when
		val card = Card(Suit.HEARTS, Rank.EIGHT)
		bout.attack(Card(Suit.SPADES, Rank.SIX))
		//then
		org.junit.jupiter.api.Assertions.assertThrows(DefendingCardException::class.java) { bout.defend(Card(Suit.SPADES,Rank.SIX), card) }
	}
	
	@Test
	fun `defender can beat a card with higher rank same suit`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		defender.cards.add(Card(Suit.SPADES, Rank.KING))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val attackCard = attacker.cards.first()
		bout.attack(attackCard)
		val defendCard = defender.cards.first()
		val success = bout.defend(attackCard, defendCard)
		
		//then
		assertThat(success).isTrue
		assertThat(defender.cards).doesNotContain(defendCard)
		assertThat(bout.getDefendStack().cards()).containsExactly(defendCard)
	}
	
	
	
	@Test
	fun `defender can beat non-trump with trump`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.KING))
		val defender = PlayerHand(PlayerId("Defender"))
		defender.cards.add(Card(Suit.HEARTS, Rank.SIX)) // HEARTS is trump
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val attackCard = attacker.cards.first()
		bout.attack(attackCard)
		val defendCard = defender.cards.first()
		val success = bout.defend(attackCard, defendCard)
		
		//then
		assertThat(success).isTrue
		assertThat(bout.isFullyDefended()).isTrue
	}
	
	@Test
	fun `defender cannot beat lower trump with non-trump`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.HEARTS, Rank.KING)) // trump that's high
		val defender = PlayerHand(PlayerId("Defender"))
		defender.cards.add(Card(Suit.SPADES, Rank.ACE)) // non-trump even though high
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val attackCard = attacker.cards.first()
		bout.attack(attackCard)
		val defendCard = defender.cards.first()
		val success = bout.defend(attackCard, defendCard)
		
		//then
		assertThat(success).isFalse
	}
	
	@Test
	fun `fully defended bout returns defender win with table cards`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		defender.cards.add(Card(Suit.SPADES, Rank.KING))
		val bout = Bout(attacker, defender, Suit.HEARTS)
		
		//when
		val attackCard = attacker.cards.first()
		bout.attack(attackCard)
		val defendCard = defender.cards.first()
		bout.defend(attackCard, defendCard)
		val result = bout.resolve()
		
		//then
		assertThat(result.defenderWon).isTrue
		assertThat(result.tableCards).containsExactlyInAnyOrder(attackCard, defendCard)
		assertThat(result.winner).isEqualTo(defender)
		
	}
	
	@Test
	fun `undefended bout returns attacker win and defender takes cards`() {
		//given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.KING))
		val defender = PlayerHand(PlayerId("Defender"))
		defender.cards.add(Card(Suit.HEARTS, Rank.KING)) // can't beat spades with non-trump hearts
		val bout = Bout(attacker, defender, Suit.CLUBS)
		
		//when
		val attackCard = attacker.cards.first()
		bout.attack(attackCard)
		val defendCard = defender.cards.first()
		val result = bout.resolve()
		var result1 = listOf(Card(Suit.SPADES, Rank.NINE))
		result1 = bout.tablePile()
		
		//then
		assertThat(result.defenderWon).isFalse
		assertThat(result.tableCards).isEmpty()
		assertThat(result.winner).isEqualTo(attacker)
		assertThat(defender.cards).contains(attackCard)
		assertThat(result1).isEmpty()
	}

	@Test
	fun `promote defended cards to attacks and finalize moves to discard`() {
		// given
		val attacker = PlayerHand(PlayerId("Attacker"))
		attacker.cards.add(Card(Suit.SPADES, Rank.SIX))
		val defender = PlayerHand(PlayerId("Defender"))
		defender.cards.add(Card(Suit.SPADES, Rank.KING))
		val bout = Bout(attacker, defender, Suit.HEARTS)

		// when: attacker plays and defender successfully defends
		val attackCard = attacker.cards.first()
		bout.attack(attackCard)
		val defendCard = defender.cards.first()
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