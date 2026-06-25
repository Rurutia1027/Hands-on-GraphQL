package dev.danvega.jbooks.book;

import dev.danvega.jbooks.author.Author;
import dev.danvega.jbooks.author.AuthorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class BookController {
    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookController(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }


    @QueryMapping
    public List<Book> books() {
        return bookRepository.findAll();
    }

    @QueryMapping
    public Optional<Book> book(@Argument Long id) {
        return bookRepository.findWithAuthorById(id);
    }

    @MutationMapping
    public Book addBook(@Argument BookInput bookInput) {
        Author author = authorRepository.findById(bookInput.authorId()).orElseThrow();
        var book = new Book();
        book.setTitle(bookInput.title());
        book.setAuthor(author);
        return bookRepository.save(book);
    }
}
