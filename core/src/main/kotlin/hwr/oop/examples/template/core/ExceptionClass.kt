package hwr.oop.examples.template.core

class DefenderDoesNotHaveCardException (message: String) : Exception(message){}

class AttackerDoesNotHaveCardException (message: String) : Exception(message){}

class AttackStackDoesNotContainCardException (message: String) : Exception(message){}

class PairingCardWasAlreadyBeenDefendedException (message: String) : Exception(message){}
