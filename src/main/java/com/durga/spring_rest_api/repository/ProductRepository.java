package com.durga.spring_rest_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.durga.spring_rest_api.entity.Product;

/**
 * Provides Spring Data JPA persistence operations for {@link Product} entities
 * identified by {@link Long} IDs.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
