package dev.danvega.jbooks.author;

import dev.danvega.jbooks.book.Book;
import dev.danvega.jbooks.book.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * scene2: resolves Author#books in a single batched query.
 */
@Controller
@Profile("scene2")
public class AuthorBooksBatchMappingController {
    private static final Logger log =
            LoggerFactory.getLogger(AuthorBooksBatchMappingController.class);
    private final BookRepository bookRepository;

    public AuthorBooksBatchMappingController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @BatchMapping
    public List<List<Book>> books(List<Author> authors) {
        log.info("Getting books for {} authors (batch)", authors.size());
        List<Long> authorIds = authors.stream()
                .map(Author::getId)
                .toList();

        List<Book> allBooks = bookRepository.findByAuthorIdIn(authorIds);
        Map<Long, List<Book>> booksByAuthorId = allBooks.stream()
                .collect(Collectors.groupingBy(book -> book.getAuthor().getId()));

        return authors.stream()
                .map(author -> booksByAuthorId.getOrDefault(author.getId(), Collections.emptyList()))
                .toList();
    }
}
