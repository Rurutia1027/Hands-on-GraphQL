package dev.danvega.jbooks;

import dev.danvega.jbooks.author.Author;
import dev.danvega.jbooks.author.AuthorRepository;
import dev.danvega.jbooks.book.Book;
import dev.danvega.jbooks.book.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public DataLoader(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadAuthorsAndBooks();
    }

    private void loadAuthorsAndBooks() {
        List<Author> authors = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Author author = new Author();
            author.setName("Author " + i);
            authors.add(authorRepository.save(author));
        }

        for (Author author : authors) {
            for (int i = 1; i <= 5; i++) {
                Book book = new Book();
                book.setTitle("Book " + i + " of " + author.getName());
                book.setAuthor(author);
                bookRepository.save(book);
            }
        }
    }
}
