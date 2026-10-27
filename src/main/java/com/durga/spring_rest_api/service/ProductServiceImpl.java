package com.durga.spring_rest_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.durga.spring_rest_api.dto.ProductRequest;
import com.durga.spring_rest_api.dto.ProductResponse;
import com.durga.spring_rest_api.entity.Product;
import com.durga.spring_rest_api.repository.ProductRepository;

/**
 * Retrieves and saves products, mapping persisted entities to API response DTOs.
 */
@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	/**
	 * Creates a service backed by the supplied repository.
	 *
	 * @param productRepository repository used to retrieve products
	 */
	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return an unmodifiable list of mapped product responses
	 */
	@Override
	public List<ProductResponse> getAllProducts() {
		List<Product> listOfProducts = productRepository.findAll();
		return listOfProducts.stream().map(this::toResponse).toList();
	}

	/** {@inheritDoc} */
	@Override
	public ProductResponse getProductById(Long id) {
		Product product = productRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));
		return toResponse(product);
	}

	/**
	 * Maps a persisted product to a response, treating null availability as false.
	 *
	 * @param product product to map
	 * @return the product response
	 */
	private ProductResponse toResponse(Product product) {
		return new ProductResponse(product.getId(), product.getName(), product.getCategory(),
				Boolean.TRUE.equals(product.getAvailable()), product.getPrice());
	}

	/** {@inheritDoc} */
	public ProductResponse createProduct(ProductRequest productRequest) {

		Product product = new Product();
		product.setName(productRequest.getName());
		product.setCategory(productRequest.getCategory());
		product.setPrice(productRequest.getPrice());
		product.setAvailable(true);

		Product productDetails = productRepository.save(product);
		return mapToResponse(productDetails);

	}

	/**
	 * Maps a saved product to a response, treating null availability as false.
	 *
	 * @param product saved product
	 * @return product details for the API response
	 */
	private ProductResponse mapToResponse(Product product) {
		return new ProductResponse(product.getId(), product.getName(), product.getCategory(),
				Boolean.TRUE.equals(product.getAvailable()), product.getPrice());
	}

	/** {@inheritDoc} */
	public ProductResponse updateProduct(Long id, ProductRequest productRequest) {

		Product product = productRepository.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));

		product.setName(productRequest.getName());
		product.setPrice(productRequest.getPrice());

		Product updatedProduct = productRepository.save(product);

		return mapToResponse(updatedProduct);

	}
}
