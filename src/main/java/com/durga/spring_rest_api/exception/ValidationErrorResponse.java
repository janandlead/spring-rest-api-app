package com.durga.spring_rest_api.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response body returned by {@link GlobalExceptionHandler} for invalid request
 * fields. The HTTP status is set separately by the handler's response entity.
 */
@Setter
@Getter
@Builder
public class ValidationErrorResponse {
	
	/** Time the error response was created in the server's local time, without an offset. */
	private LocalDateTime timestamp;
	/** Numeric HTTP status, currently 400 for request validation failures. */
	private int status;
	/** Summary of the failure, currently {@code Validation Failed}. */
	private String error;
	/** Field names mapped to one constraint message per field. */
	private Map<String, String> validationErrors;

}
