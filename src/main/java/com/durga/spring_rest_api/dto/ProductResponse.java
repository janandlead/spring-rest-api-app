package com.durga.spring_rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product details returned by the product API.
 * Missing database availability is represented as {@code false} by the service.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
	
	/** Product identifier. */
	private Long id;
	/** Product name. */
	private String name;
	/** Product category. */
	private String category;
	/** Whether the product is available. */
	private boolean available;
	/** Product price. */
	private double price;
	

}
