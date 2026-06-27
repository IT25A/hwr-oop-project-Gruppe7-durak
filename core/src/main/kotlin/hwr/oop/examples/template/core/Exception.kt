package hwr.oop.examples.template.core

class DefenderDoesNotHaveCardException(attackingCard: String) : Exception("Defender does not have the card: $attackingCard") {}

class AttackerDoesNotHaveCardException(card: String) : Exception("Attacker does not have the card: $card") {}

class AttackStackDoesNotContainCardException(attackingCard: String) : Exception("Attack stack does not contain the attacking card: $attackingCard") {}

class PairingCardWasAlreadyBeenDefendedException(attackingCard: String) : Exception("The attacking card has already been defended: $attackingCard") {}

class InvalidPlayerNumberException(playerCount: String) : Exception("Player count must be between 2 and 4, but was $playerCount") {}

class AttackerNotFoundException() : Exception("Attacker not found") {}

class NoActiveBoutException() : Exception("No active bout") {}

class NoActiveRoundException() : Exception("No active round") {}

class RankNotOnTableException() : Exception("Card rank does not match any rank on the table") {}

class AttackerAndDefenderCanNotJoinAttackException() : Exception("Attacker and defender cannot join the attack") {}

class AttackerCanNotJoinHisAttackException() : Exception("Player already joined the attack") {}

class JoinerNotFoundException() : Exception("Player not found") {}

class DefenderNotFoundException() : Exception("Defender not found") {}

class JoinerDoesNotHaveCardException() : Exception("Joiner doesn't have the card") {}

class DefenderDoesNotHaveEnoughCardsException() : Exception("Defender does not have enough cards") {}
class CardDoesNotBeatAttackingCardException(attackingCard: String, defendingCard: String) :
	Exception("Defending card $defendingCard does not beat attacking card $attackingCard")
