package com.durga.spring_rest_api.dto;

/**
 * Immutable representation of a product. The current controller returns
 * {@link ProductResponse} instead.
 *
 * @param id product identifier
 * @param name product name
 * @param category product category
 * @param available whether the product is available
 * @param price product price
 */
public record ProductResponseDto(Long id, String name, String category, boolean available, double price) {

}
