package dev.danvega.jbooks.author;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 * scene2: BatchMapping batches all author book lookups into one IN query.
 * <p>
 * Prerequisite for Grafana: {@code docker compose up -d grafana-lgtm}
 */

@Tag("tracing")
@ActiveProfiles({"scene2", "tracing", "test"})
public class AuthorBooksScene2TracingTest extends AuthorBooksTracingTestSupport {

    @Test
    void singleAuthorWithBooks_batchMapping_triggersTwoQueries() {
        // 1x single author + 1x books IN query with one id
        runSingleAuthorWithBooksScenario("scene2-batch-mapping-single-author", 2, 4);
    }

    @Test
    void allAuthorsWithBooks_batchMapping_triggersBatchedQueries() {
        // 1x authors + 1x books IN query for all 1000 authors (+ small Hibernate overhead)
        runAuthorsWithBooksScenario("scene2-batch-mapping-all-authors", 2, 4);
    }
}
