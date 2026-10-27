package com.durga.spring_rest_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the Spring application context starts with the configured dependencies.
 * Requires access to the configured database.
 */
@SpringBootTest
class SpringRestApiApplicationTests {

	/** Verifies application context initialization without startup errors. */
	@Test
	void contextLoads() {
	}

}
