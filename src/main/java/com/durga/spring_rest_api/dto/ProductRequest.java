package com.durga.spring_rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request details for creating or updating a product.
 * Creation uses name, category, and price; updates use only name and price.
 * Neither operation uses the requested availability.
 * No Bean Validation constraints are currently declared.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
	
	/** Product name used by both create and update operations. */
	private String name;
	/** Category used only when creating a product. */
	private String category;
	/** Legacy misspelled availability field, currently ignored by the service. */
	private boolean isAvialable;
	/** Price; must be non-null when the service copies it to the primitive entity field. */
	private Double price;

}
