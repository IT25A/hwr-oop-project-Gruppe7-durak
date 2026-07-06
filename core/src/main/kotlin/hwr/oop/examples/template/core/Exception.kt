package hwr.oop.examples.template.core

open class GameRuleException(message: String) : RuntimeException(message)

class DefenderDoesNotHaveCardException(attackingCard: String) :
	GameRuleException("Defender does not have the card: $attackingCard") {}

class AttackerDoesNotHaveCardException(card: String) : GameRuleException("Attacker does not have the card: $card") {}

class AttackStackDoesNotContainCardException(attackingCard: String) :
	GameRuleException("Attack stack does not contain the attacking card: $attackingCard") {}

class PairingCardWasAlreadyBeenDefendedException(attackingCard: String) :
	GameRuleException("The attacking card has already been defended: $attackingCard") {}

class InvalidPlayerNumberException(playerCount: String) :
	GameRuleException("Player count must be between 2 and 6, but was $playerCount") {}

class AttackerNotFoundException() : GameRuleException("Attacker not found") {}

class NoActiveBoutException() : GameRuleException("No active bout") {}

class NoActiveRoundException() : GameRuleException("No active round") {}

class RankNotOnTableException() : GameRuleException("Card rank does not match any rank on the table") {}

class AttackerAndDefenderCanNotJoinAttackException() : GameRuleException("Attacker and defender cannot join the attack") {}

class AttackerCanNotJoinHisAttackException() : GameRuleException("Player already joined the attack") {}

class JoinerNotFoundException() : GameRuleException("Player not found") {}

class DefenderNotFoundException() : GameRuleException("Defender not found") {}

class JoinerDoesNotHaveCardException() : GameRuleException("Joiner doesn't have the card") {}

class DefenderDoesNotHaveEnoughCardsException() : GameRuleException("Defender does not have enough cards") {}

class DefenderCanNotPassException() : GameRuleException("Defender cannot pass on supplying") {}

class PlayerAlreadyPassedSupplyException(playerId: String) :
	GameRuleException("Player already passed on supplying: $playerId") {}

class CardDoesNotBeatAttackingCardException(attackingCard: String, defendingCard: String) :
	GameRuleException("Defending card $defendingCard does not beat attacking card $attackingCard")

class GameNotFoundException(gameID: String) : Exception("Game with ID $gameID not found") {}
