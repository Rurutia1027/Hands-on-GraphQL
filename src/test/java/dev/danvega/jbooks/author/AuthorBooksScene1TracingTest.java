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
    void singleAuthorWithBooks_schemaMapping_triggersTwoQueries() {
        // 1x single author + 1x books by author_id
        runSingleAuthorWithBooksScenario("scene1-single-author", 2, 3);
    }

    @Test
    void allAuthorsWithBooks_schemaMapping_triggersNPlusOneQueries() {
        // 1x authors + 20x books (one per author)
        runAuthorsWithBooksScenario("scene1-all-authors", 21, 30);
    }
}
