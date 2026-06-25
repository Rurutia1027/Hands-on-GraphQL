package dev.danvega.jbooks.author;

import dev.danvega.jbooks.book.Book;
import dev.danvega.jbooks.book.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * scene1: resolves Author.books per author (N+1 queries).
 */
@Controller
@Profile("scene1")
public class AuthorBooksSchemaMappingController {

    private static final Logger log = LoggerFactory.getLogger(AuthorBooksSchemaMappingController.class);
    private final BookRepository bookRepository;

    public AuthorBooksSchemaMappingController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @SchemaMapping
    public List<Book> books(Author author) {
        log.info("Getting books for author id={}", author.getId());
        return bookRepository.findByAuthorId(author.getId());
    }
}
