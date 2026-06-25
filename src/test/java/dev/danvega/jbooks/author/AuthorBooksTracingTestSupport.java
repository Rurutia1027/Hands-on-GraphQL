package dev.danvega.jbooks.author;

import dev.danvega.jbooks.support.PostgresTestcontainersConfiguration;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
abstract public class AuthorBooksTracingTestSupport extends PostgresTestcontainersConfiguration {
    private static final Logger LOG =
            LoggerFactory.getLogger(AuthorBooksTracingTestSupport.class);

    protected static final String AUTHORS_WITH_BOOKS_QUERY = """
             query AuthorsWithBooks {
              authors {
                id
                name
                books {
                  id
                  title
                }
              }
            }
            """;
    protected static final int AUTHOR_COUNT = 6;

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private Tracer tracer;

    @BeforeEach
    void resetStatistics() {
        statistics().clear();
    }

    protected void runAuthorsWithBooksScenario(String scenario, int expectedMinStatements,
                                               int expectedMaxStatements) {
        Span span = tracer.nextSpan()
                .name(scenario)
                .tag("test.scenario", scenario)
                .start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            statistics().clear();
            graphQlTester.document(AUTHORS_WITH_BOOKS_QUERY)
                    .execute()
                    .path("authors")
                    .entityList(Object.class)
                    .hasSize(AUTHOR_COUNT);
            long statementCount = statistics().getPrepareStatementCount();
            String traceId = span.context().traceId();

            assertThat(statementCount)
                    .as("prepared SQL statements for %s", scenario)
                    .isBetween((long) expectedMinStatements, (long) expectedMaxStatements);

            logGrafanaLookupHint(scenario, traceId, statementCount, expectedMinStatements,
                    expectedMaxStatements);
        } finally {
            span.end();
            waitForTraceExport();
        }
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    private void waitForTraceExport() {
        try {
            Thread.sleep(2_000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void logGrafanaLookupHint(
            String scenario,
            String traceId,
            long statementCount,
            int expectedMin,
            int expectedMax) {
        LOG.info("""
                        
                        ================================================================================
                        Scenario : {}
                        Trace ID : {}  (Grafana -> Explore -> Tempo -> TraceQL: {{ trace_id = "{}" }})
                        SQL count: {} prepared statements (expected {}-{})
                        Grafana  : http://localhost:3000
                        Tip      : open the trace and filter child spans by name "SELECT" / db.statement
                        ================================================================================
                        """,
                scenario,
                traceId,
                traceId,
                statementCount,
                expectedMin,
                expectedMax);
    }

}
