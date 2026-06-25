package dev.danvega.jbooks.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class PostgresTestcontainersConfiguration {

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        // External PostgreSQL (for local docker image / docker compose)
        registry.add("spring.datasource.url",
                () -> System.getProperty(
                        "test.db.url",
                        System.getenv().getOrDefault("TEST_DB_URL", "jdbc:postgresql://localhost:5432/jbooks")
                ));
        registry.add("spring.datasource.username",
                () -> System.getProperty(
                        "test.db.username",
                        System.getenv().getOrDefault("TEST_DB_USERNAME", "admin")
                ));
        registry.add("spring.datasource.password",
                () -> System.getProperty(
                        "test.db.password",
                        System.getenv().getOrDefault("TEST_DB_PASSWORD", "admin")
                ));
    }
}
