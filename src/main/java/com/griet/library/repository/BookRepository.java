package com.griet.library.repository;

import com.griet.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // search books by title (case insensitive)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // find books by branch
    List<Book> findByBranch(String branch);

    // find books recommended for a particular year
    List<Book> findByRecommendedYear(int year);

    // find books belonging to multiple categories
    List<Book> findByCategoryIn(List<String> categories);

    // find book by accession number
    Book findByAccessionNumber(String accessionNumber);

    // find book by ISBN
    Book findByIsbn(String isbn);

    // find only available books
    List<Book> findByAvailableTrue();

}
