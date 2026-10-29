package com.durga.spring_rest_api.service;

import java.util.List;

import com.durga.spring_rest_api.dto.ProductRequest;
import com.durga.spring_rest_api.dto.ProductResponse;

/**
 * Defines product retrieval, creation, and update operations for the API layer.
 */
public interface ProductService {

	/**
	 * Retrieves products by category using a derived query.
	 * @param category category to match by equality
	 * @return matching product responses, or an empty list
	 */
	List<ProductResponse> getProductsByCategory(String category);

	/**
	 * Retrieves products by category using JPQL.
	 * @param category category to match by equality
	 * @return matching product responses, or an empty list
	 */
	List<ProductResponse> getProductsByCategoryJpql(String category);

	/**
	 * Retrieves products by name using native SQL.
	 * @param name product name to match by equality
	 * @return matching product responses across categories, or an empty list
	 */
	List<ProductResponse> getProductsByNameNative(String name);

	/**
	 * Retrieves products matching both category and name using JPQL.
	 * @param category category to match by equality
	 * @param name product name to match by equality
	 * @return product responses matching both values, or an empty list
	 */
	List<ProductResponse> getProductsByCategoryAndName(String category, String name);
	
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
	
	
	/**
	 * Deletes an existing product.
	 * @param id identifier of the product to delete
	 * @throws com.durga.spring_rest_api.exception.ProductNotFoundException if no product exists
	 */
	void deleteProduct(Long id);
}
