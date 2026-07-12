package hwr.oop.examples.template.service

import hwr.oop.examples.template.core.GameNotFoundException
import hwr.oop.examples.template.core.GameRuleException
import hwr.oop.examples.template.service.model.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class ControllerAdvice {
	
	@ExceptionHandler(Exception::class)
	fun handleGenericException(
		ex: Exception,
		request: WebRequest,
	): ResponseEntity<ErrorResponse> {
		val errorResponse = ErrorResponse(
			/*status =*/ HttpStatus.INTERNAL_SERVER_ERROR.value(),
			/*error =*/ HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
			/*message =*/ ex.message ?: "An unexpected error occurred",
		)
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(errorResponse)
	}
	
	@ExceptionHandler(GameNotFoundException::class)
	fun handleGameNotFoundException(ex: GameNotFoundException): ResponseEntity<ErrorResponse> {
		val errorResponse = ErrorResponse(
			/*status =*/ HttpStatus.NOT_FOUND.value(),
			/*error =*/ HttpStatus.NOT_FOUND.reasonPhrase,
			/*message =*/ ex.message ?: "Not found",
		)
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(errorResponse)
	}

	@ExceptionHandler(GameRuleException::class)
	fun handleGameRuleException(ex: GameRuleException): ResponseEntity<ErrorResponse> {
		val errorResponse = ErrorResponse(
			/*status =*/ HttpStatus.BAD_REQUEST.value(),
			/*error =*/ HttpStatus.BAD_REQUEST.reasonPhrase,
			/*message =*/ ex.message ?: "Bad request",
		)
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(errorResponse)
	}

	@ExceptionHandler(IllegalArgumentException::class)
	fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
		val errorResponse = ErrorResponse(
			/*status =*/ HttpStatus.BAD_REQUEST.value(),
			/*error =*/ HttpStatus.BAD_REQUEST.reasonPhrase,
			/*message =*/ ex.message ?: "Bad request",
		)
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(errorResponse)
	}
	
}
