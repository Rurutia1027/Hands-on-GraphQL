package dev.danvega.jbooks.author;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 * scene1: SchemaMapping triggers one books query per author (N+1)
 * <p>
 * Prerequisite for Grafana: {@code docker compose up -d grafana-lgtm}
 */
@Tag("tracing")
@ActiveProfiles({"scene1", "tracing", "test"})
public class AuthorBooksScene1TracingTest extends AuthorBooksTracingTestSupport {
    @Test
    void authorsWithBooks_schemaMapping_triggersNPlusOneQueries() {
        // 1x authors + 6x books (one per author)
        runAuthorsWithBooksScenario("scene1-schema-mapping-n-plus-one", 7, 10);
    }
}
