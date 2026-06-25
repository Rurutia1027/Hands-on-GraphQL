package dev.danvega.jbooks.book;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Collection<?> findAllByTitleContainsIgnoreCase(String text);

    List<Book> findByAuthorId(Long authorId);

    List<Book> findByAuthorIdIn(List<Long> authorIds);

    @EntityGraph(attributePaths = "author")
    Optional<Book> findWithAuthorById(Long id);
}
