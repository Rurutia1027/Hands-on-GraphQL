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

import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureGraphQlTester
abstract public class AuthorBooksTracingTestSupport extends PostgresTestcontainersConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorBooksTracingTestSupport.class);

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
    protected static final String AUTHOR_WITH_BOOKS_QUERY = """
            query AuthorWithBooks($id: ID!) {
              author(id: $id) {
                id
                name
                books {
                  id
                  title
                }
              }
            }
            """;
    protected static final long SINGLE_AUTHOR_ID = 1L;
    protected static final int AUTHOR_COUNT = 1000;

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
        runScenario(
                scenario,
                expectedMinStatements,
                expectedMaxStatements,
                tester -> tester.document(AUTHORS_WITH_BOOKS_QUERY)
                        .execute()
                        .path("authors")
                        .entityList(Object.class)
                        .hasSize(AUTHOR_COUNT)
        );
    }

    protected void runSingleAuthorWithBooksScenario(String scenario, int expectedMinStatements,
                                                    int expectedMaxStatements) {
        runScenario(
                scenario,
                expectedMinStatements,
                expectedMaxStatements,
                tester -> tester.document(AUTHOR_WITH_BOOKS_QUERY)
                        .variable("id", SINGLE_AUTHOR_ID)
                        .execute()
                        .path("author.id")
                        .entity(String.class)
                        .isEqualTo(String.valueOf(SINGLE_AUTHOR_ID))
        );
    }

    private void runScenario(
            String scenario,
            int expectedMinStatements,
            int expectedMaxStatements,
            Consumer<GraphQlTester> assertion) {
        Span span = tracer.nextSpan()
                .name(scenario)
                .tag("test.scenario", scenario)
                .start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            statistics().clear();
            assertion.accept(graphQlTester);
            long statementCount = statistics().getPrepareStatementCount();
            String traceId = span.context().traceId();

            assertThat(statementCount)
                    .as("prepared SQL statements for %s", scenario)
                    .isBetween((long) expectedMinStatements, (long) expectedMaxStatements);

            logGrafanaLookupHint(scenario, traceId, statementCount, expectedMinStatements,
                    expectedMaxStatements);
        } finally {
            span.end();
        }
    }

    private Statistics statistics() {
        return entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
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
                        Trace ID : {}  (Grafana -> Explore -> Tempo -> paste id or {{ trace:id = "{}" }})
                        SQL count: {} prepared statements (expected {}-{})
                        Grafana  : http://localhost:3000
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
