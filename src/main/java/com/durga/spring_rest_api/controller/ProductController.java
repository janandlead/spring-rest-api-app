package com.durga.spring_rest_api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.durga.spring_rest_api.dto.ProductRequest;
import com.durga.spring_rest_api.dto.ProductResponse;
import com.durga.spring_rest_api.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Exposes product retrieval, creation, update, and deletion operations at
 * {@code /api/v1/products}.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	/**
	 * Handles GET /api/v1/products/search/derived using equality matching.
	 * @param category required category query parameter
	 * @return matching product responses, or an empty list (HTTP 200)
	 */
	@GetMapping("/search/derived")
	public List<ProductResponse> searchDerived(@RequestParam("category") String category) {
		return productService.getProductsByCategory(category);
	}

	/**
	 * Handles GET /api/v1/products/search/jpql using equality matching.
	 * @param category required category query parameter
	 * @return matching product responses, or an empty list (HTTP 200)
	 */
	@GetMapping("/search/jpql")
	public List<ProductResponse> searchJpql(@RequestParam("category") String category) {
		return productService.getProductsByCategoryJpql(category);
	}

	/**
	 * Handles GET /api/v1/products/search/native using equality matching.
	 * @param name required name query parameter
	 * @return matching product responses, or an empty list (HTTP 200)
	 */
	@GetMapping("/search/native")
	public List<ProductResponse> searchNative(@RequestParam("name") String name) {
		return productService.getProductsByNameNative(name);
	}

	/**
	 * Handles GET /api/v1/products/search/category-and-name using equality matching.
	 * @param category required category query parameter
	 * @param name required name query parameter
	 * @return matching product responses, or an empty list (HTTP 200)
	 */
	@GetMapping("/search/category-and-name")
	public List<ProductResponse> searchCategoryAndName(@RequestParam("category") String category,
			@RequestParam("name") String name) {
		return productService.getProductsByCategoryAndName(category, name);
	}

	/**
	 * Handles {@code GET /api/v1/products}.
	 *
	 * @return all products, or an empty list when no products exist
	 */
	@GetMapping
	public List<ProductResponse> getProducts() {
		return productService.getAllProducts();

	}

	/**
	 * Handles {@code GET /api/v1/products/{id}}. Spring converts the path segment
	 * to a {@link Long}; a nonnumeric value produces HTTP 400 before the service is
	 * called.
	 *
	 * @param id product identifier from the URL path
	 * @return the matching product
	 * @throws org.springframework.web.server.ResponseStatusException if no product
	 *                                                                exists (HTTP
	 *                                                                404)
	 */
	@GetMapping("/{id}")
	public ProductResponse getProductById(@PathVariable("id") Long id) {
		return productService.getProductById(id);
	}

	/**
	 * Handles {@code POST /api/v1/products}, creating an available product.
	 * <p>
	 * Spring applies {@link Valid} to the bound request before invoking this method.
	 * Invalid names produce a request validation exception handled by
	 * {@link com.durga.spring_rest_api.exception.GlobalExceptionHandler} as HTTP 400.
	 * Price has no validation constraint but must be non-null for the service.
	 *
	 * @param productRequest JSON product details; price must be non-null
	 * @return the saved product, including its generated ID (HTTP 200)
	 */
	@PostMapping
	public ProductResponse addProduct(@Valid @RequestBody ProductRequest productRequest) {
		return productService.createProduct(productRequest);

	}

	/**
	 * Handles {@code PUT /api/v1/products/id/{id}}. Updates name and price;
	 * category and availability retain their stored values.
	 * <p>
	 * This parameter does not use {@link Valid}, so the DTO's name constraints are
	 * not checked by request-body validation for this endpoint.
	 *
	 * @param id             identifier of the product to update
	 * @param productRequest replacement name and non-null price
	 * @return HTTP 200 with the updated product
	 * @throws org.springframework.web.server.ResponseStatusException if the product
	 *                                                                does not exist
	 */
	@PutMapping("/id/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") Long id,
			@RequestBody ProductRequest productRequest) {

		return ResponseEntity.ok(productService.updateProduct(id, productRequest));

	}

	/**
	 * Handles {@code DELETE /api/v1/products/product/{id}} by deleting an existing
	 * product.
	 *
	 * @param id identifier of the product to delete
	 * @throws org.springframework.web.server.ResponseStatusException if no product
	 *         exists (HTTP 404)
	 */
	@DeleteMapping("/product/{id}")
	public void deleteProduct(@PathVariable("id") Long id) {

		productService.deleteProduct(id);

	}
}
