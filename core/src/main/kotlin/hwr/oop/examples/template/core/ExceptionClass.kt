package hwr.oop.examples.template.core

class DefenderDoesNotHaveCardException(message: String) : Exception(message) {}

class AttackerDoesNotHaveCardException(message: String) : Exception(message) {}

class AttackStackDoesNotContainCardException(message: String) : Exception(message) {}

class PairingCardWasAlreadyBeenDefendedException(message: String) : Exception(message) {}

class InvalidPlayerNumberException(message: String) : Exception(message) {}

class AttackerNotFoundException(message: String) : Exception(message) {}

class NoActiveBoutException(message: String) : Exception(message) {}

class NoActiveRoundException(message: String) : Exception(message) {}

class RankNotOnTableException(message: String) : Exception(message) {}

class AttackerAndDefenderCanNotJoinAttackException(message: String) : Exception(message) {}

class AttackerCanNotJoinHisAttackException(message: String) : Exception(message) {}

class JoinerNotFoundException(message: String) : Exception(message) {}

class DefenderNotFoundException(message: String) : Exception(message) {}

class JoinerDoesNotHaveCardException(message: String) : Exception(message) {}

class DefenderDoesNotHaveEnoughCardsException(message: String) : Exception(message) {}