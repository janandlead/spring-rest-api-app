package com.durga.spring_rest_api.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Supplies shared HTTP error handling for REST controllers.
 * <p>
 * Handles request-body Bean Validation failures only. Other exceptions, including
 * {@link org.springframework.web.server.ResponseStatusException}, remain subject
 * to Spring's other exception resolvers and error handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Converts a request-body validation failure into an HTTP 400 response.
	 * <p>
	 * Collects field names and constraint messages from the binding result. Only
	 * one message is retained per field: subsequent errors for that field overwrite
	 * earlier ones. Object-level errors are not included.
	 *
	 * @param ex validation exception raised by Spring before controller invocation
	 * @return HTTP 400 with a server-local timestamp and field validation messages
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

		Map<String, String> erros = new HashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(error -> erros.put(error.getField(), error.getDefaultMessage()));

		ValidationErrorResponse validationErrorResponse = ValidationErrorResponse.builder()
				.timestamp(LocalDateTime.now()).status(HttpStatus.BAD_REQUEST.value()).error("Validation Failed")
				.validationErrors(erros).build();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationErrorResponse);

	}

}
