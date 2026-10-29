package com.durga.spring_rest_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request details for creating or updating a product.
 * Creation uses name, category, and price; updates use only name and price.
 * Neither operation uses the requested availability.
 * <p>
 * Name constraints are checked when Spring validates this DTO through
 * {@link jakarta.validation.Valid}. The POST controller activates this validation;
 * the PUT controller currently does not. Category and price have no Bean
 * Validation constraints.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
	
	/**
	 * Product name used by create and update operations. When validated, it must
	 * be nonblank and contain 3 through 10 characters; validation does not trim it.
	 */
	@NotBlank(message="Name is required")
	@Size(min=3, max=10, message="Name must be 3-10 charectors")
	private String name;
	/** Category used only when creating a product. */
	private String category;
	/** Legacy misspelled availability field, currently ignored by the service. */
	private boolean isAvialable;
	/**
	 * Price copied to the primitive entity field. No validation constraint enforces
	 * a value or range; a null value causes an unboxing error in the service.
	 */
	private Double price;

}
