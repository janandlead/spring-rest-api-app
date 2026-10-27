package com.durga.spring_rest_api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.durga.spring_rest_api.dto.ProductResponse;
import com.durga.spring_rest_api.entity.Product;
import com.durga.spring_rest_api.repository.ProductRepository;

/** Verifies product lookup and missing-product handling without a database. */
class ProductServiceImplTests {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductServiceImpl service = new ProductServiceImpl(repository);

    /** Verifies all response fields and null availability for a matching ID. */
    @Test
    void returnsProductWithNullAvailabilityAsFalse() {
        Product product = new Product();
        product.setId(7L);
        product.setName("Keyboard");
        product.setCategory("Electronics");
        product.setAvailable(null);
        product.setPrice(49.5);
        when(repository.findById(7L)).thenReturn(Optional.of(product));

        ProductResponse response = service.getProductById(7L);

        assertEquals(7L, response.getId());
        assertEquals("Keyboard", response.getName());
        assertEquals("Electronics", response.getCategory());
        assertEquals(49.5, response.getPrice());
        assertFalse(response.isAvailable());
    }

    /** Verifies that a missing ID produces HTTP 404 instead of a null response. */
    @Test
    void throwsNotFoundForMissingProduct() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getProductById(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
