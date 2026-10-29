package com.durga.spring_rest_api.exception;

public class ProductNotFoundException extends RuntimeException{
	
	public ProductNotFoundException(String message) {
		super(message);
	}

}
