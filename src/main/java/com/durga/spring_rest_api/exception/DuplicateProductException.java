package com.durga.spring_rest_api.exception;

public class DuplicateProductException extends RuntimeException{

	public DuplicateProductException(String message) {
		super(message);
	}
}
