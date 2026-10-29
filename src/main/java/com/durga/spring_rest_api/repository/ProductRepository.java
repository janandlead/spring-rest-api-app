package com.durga.spring_rest_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.durga.spring_rest_api.entity.Product;

/**
 * Provides Spring Data JPA persistence operations for {@link Product} entities
 * identified by {@link Long} IDs.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

	/**
	 * Finds products by category using a derived query.
	 * @param category category to match by equality
	 * @return matching products, or an empty list
	 */
	List<Product> findByCategory(String category);

	/**
	 * Finds products by category using JPQL.
	 * @param category category to match by equality
	 * @return matching products, or an empty list
	 */
	@Query("SELECT e FROM Product e WHERE e.category=:category") // JPQL uses the entity name with named parameter
	List<Product> getProductCategory(@Param("category") String category);

	/**
	 * Finds products by name using native SQL.
	 * @param name product name to match by equality
	 * @return matching products across all categories, or an empty list
	 */
	@Query(value = "select * from products where name=:name", nativeQuery = true) // Native Query
	List<Product> gerProductName(@Param("name") String name);

	
	
	/**
	 * Finds products matching both category and name using JPQL.
	 * @param category category to match by equality
	 * @param name product name to match by equality
	 * @return products matching both values, or an empty list
	 */
	@Query("SELECT e FROM Product e WHERE e.category=?1 AND e.name=?2") // Positional parameter
	List<Product> getProductCategoryAndName(String category, String name);
}
