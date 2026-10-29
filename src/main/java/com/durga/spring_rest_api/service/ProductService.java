package com.durga.spring_rest_api.service;

import java.util.List;

import com.durga.spring_rest_api.dto.ProductRequest;
import com.durga.spring_rest_api.dto.ProductResponse;

/**
 * Defines product retrieval, creation, and update operations for the API layer.
 */
public interface ProductService {
	
	/**
	 * Retrieves all products as response DTOs.
	 * Database availability values of {@code null} are represented as {@code false}.
	 *
	 * @return all product responses, or an empty list when no products exist
	 */
	List<ProductResponse>  getAllProducts();

	/**
	 * Retrieves a product by its identifier, mapping null availability to false.
	 *
	 * @param id product identifier, must not be null
	 * @return the matching product response
	 * @throws org.springframework.web.server.ResponseStatusException if no product exists (HTTP 404)
	 */
	ProductResponse getProductById(Long id);
	
	
	
	/**
	 * Persists a new product using the request's name, category, and price.
	 * Availability is always set to true, regardless of the request value.
	 *
	 * @param productRequest product details with a non-null price
	 * @return the saved product with its generated identifier
	 */
	ProductResponse createProduct(ProductRequest productRequest);
	
	/**
	 * Replaces an existing product's name and price, preserving category and availability.
	 *
	 * @param id identifier of the product to update, must not be null
	 * @param productRequest replacement name and non-null price
	 * @return the saved product response
	 * @throws org.springframework.web.server.ResponseStatusException if no product exists (HTTP 404)
	 */
	ProductResponse updateProduct(Long id,ProductRequest productRequest);
	
	
	void deleteProduct(Long id);
}
