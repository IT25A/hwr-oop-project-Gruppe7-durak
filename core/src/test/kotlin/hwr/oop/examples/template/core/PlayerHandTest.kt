package hwr.oop.examples.template.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PlayerHandTest {
	
	@Test
	fun `player hand can have an ID`() { val expected = PlayerId("Alpha")
		
		//when
		val hand = PlayerHand.create(emptyList(), expected)
		
		// prüft direkt die Getter-Rückgabe
		assertThat(hand.getId()).isEqualTo(expected)
	}
	@Test
	fun `player hand id is not empty`() {
		val hand = PlayerHand(PlayerId("Alpha"), cardsInternal = mutableListOf())
		
		// falls eine Mutation die id als leeren Wert zurückgibt, schlägt dieser Test fehl
		assertThat(hand.getId()).isNotEqualTo(PlayerId(""))
	}
	
	@Test
	fun `testplayer hand receives cards from mutable deck`() {
		
		val deck = Deck.createRandomDeck().toMutableDeck()
		var playerHand = PlayerHand(PlayerId("test"), cardsInternal = mutableListOf())
		val amountToDeal = 6
		
		// 2. Aktion: Die Hand erhält Karten über das Deck
		playerHand = deck.dealTo(playerHand, amountToDeal)
		
		// 3. Assertions
		
		// Überprüfen, ob die Hand jetzt 6 Karten hat
		assertThat(playerHand.cards().size).isEqualTo(amountToDeal)
		
		// Überprüfen, ob die Karten in der Hand Instanzen der Klasse Card sind
		assertThat(playerHand.cards().all { it is Card }).isEqualTo(true)
	}
		
		@Test
		fun `create with defaults returns empty hand and empty id`() {
			// when: call create with no arguments (uses default parameters)
			val hand = PlayerHand.create()
			
			// then: id is default empty PlayerId and cards list is empty
			assertThat(hand.getId()).isEqualTo(PlayerId(""))
			assertThat(hand.cards()).isEmpty()
		}
		
		@Test
		fun `create with explicit cards and id copies the list (immutable)`() {
			// given: a mutable source list and a chosen id
			val card1 = Card(Suit.SPADES, Rank.SIX)
			val card2 = Card(Suit.HEARTS, Rank.SEVEN)
			val source = mutableListOf(card1, card2)
			val id = PlayerId("Player X")
			
			// when: create a PlayerHand from that source list
			val hand = PlayerHand.create(cards = source, id = id)
			
			// then: the hand has the provided id and contains the cards
			assertThat(hand.getId()).isEqualTo(id)
			assertThat(hand.cards()).containsExactly(card1, card2)
			
			// and: modifying the original source list does NOT affect the created hand
			source.add(Card(Suit.CLUBS, Rank.EIGHT))
			assertThat(source.size).isEqualTo(3) // sanity check
			assertThat(hand.cards()).hasSize(2) // the created hand still has 2 cards
		}
	}