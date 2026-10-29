package com.durga.spring_rest_api.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
	
	/** Time the error response was created in the server's local time, without an offset. */
	private LocalDateTime timestamp;
	/** Numeric HTTP status, currently 400 for request validation failures. */
	private int status;
	/** Summary of the failure, currently {@code Validation Failed}. */
	private String error;
	
	private String message;
	private String path;

}
