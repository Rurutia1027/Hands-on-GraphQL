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
    void authorsWithBooks_batchMapping_triggersBatchedQueries() {
        // 1x author + 1x books IN query (+small Hibernate overhead)
        runAuthorsWithBooksScenario("scene2-batch-mapping", 2, 4);
    }
}
