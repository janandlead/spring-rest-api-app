package com.durga.spring_rest_api;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.durga.spring_rest_api.entity.Product;
import com.durga.spring_rest_api.repository.ProductRepository;

/** Exercises HTTP routing, DTO mapping and actual queries against an isolated H2 database. */
@SpringBootTest
@Transactional
class ProductSearchTests {
    @Autowired WebApplicationContext context;
    @Autowired ProductRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        save("Keyboard", "Electronics", null);
        save("Mouse", "Electronics", true);
        save("Keyboard", "Office", false);
        repository.flush();
    }

    private void save(String name, String category, Boolean available) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(49.5);
        product.setAvailable(available);
        repository.save(product);
    }

    @ParameterizedTest
    @ValueSource(strings = {"derived", "jpql"})
    void categoryQueriesReturnAllMatches(String style) throws Exception {
        mvc.perform(get("/api/v1/products/search/" + style).param("category", "Electronics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder("Keyboard", "Mouse")))
            .andExpect(jsonPath("$[*].available", containsInAnyOrder(false, true)))
            .andExpect(jsonPath("$[0].id").isNumber())
            .andExpect(jsonPath("$[0].price").value(49.5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"derived", "jpql", "native", "category-and-name"})
    void noMatchesReturnEmptyList(String style) throws Exception {
        mvc.perform(get("/api/v1/products/search/" + style)
                .param("category", "Missing").param("name", "Missing"))
            .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"derived", "jpql", "native", "category-and-name"})
    void searchParameterIsRequired(String style) throws Exception {
        mvc.perform(get("/api/v1/products/search/" + style))
            .andExpect(status().isBadRequest());
    }

    @Test
    void nativeQueryMatchesNameAcrossCategories() throws Exception {
        mvc.perform(get("/api/v1/products/search/native").param("name", "Keyboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[*].name", containsInAnyOrder("Keyboard", "Keyboard")))
            .andExpect(jsonPath("$[*].category", containsInAnyOrder("Electronics", "Office")));
        mvc.perform(get("/api/v1/products/search/native").param("category", "Electronics"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void combinedQueryRequiresBothFieldsToMatch() throws Exception {
        mvc.perform(get("/api/v1/products/search/category-and-name")
                .param("category", "Electronics").param("name", "Keyboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Keyboard"))
            .andExpect(jsonPath("$[0].category").value("Electronics"));
        mvc.perform(get("/api/v1/products/search/category-and-name").param("category", "Electronics"))
            .andExpect(status().isBadRequest());
    }
}
