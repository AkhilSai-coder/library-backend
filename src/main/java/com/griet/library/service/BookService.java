package com.griet.library.service;

import com.griet.library.dto.BookDTO;
import com.griet.library.model.Book;
import com.griet.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

// ==============================
// ADD BOOK (ADMIN)
// ==============================

    public Book addBook(BookDTO dto) {

        Book book = new Book();

        book.setAccessionNumber(dto.getAccessionNumber());
        book.setTitle(dto.getTitle());
        book.setAuthors(dto.getAuthors());
        book.setPublisher(dto.getPublisher());
        book.setPlaceOfPublication(dto.getPlaceOfPublication());
        book.setYear(dto.getYear());
        book.setIsbn(dto.getIsbn());
        book.setPages(dto.getPages());
        book.setSource(dto.getSource());
        book.setPrice(BigDecimal.valueOf(dto.getPrice()));
        book.setBillNo(dto.getBillNo());
        book.setBillDate(dto.getBillDate());
        book.setType(dto.getType());

        // recommendation system fields
        book.setCategory(dto.getCategory());
        book.setBranch(dto.getBranch());
        book.setRecommendedYear(dto.getRecommendedYear());

        book.setAvailable(true);

        return bookRepository.save(book);
    }

// ==============================
// GET ALL BOOKS
// ==============================

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

// ==============================
// SEARCH BOOKS BY TITLE
// ==============================

    public List<Book> searchBooks(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

// ==============================
// GET BOOK BY ID
// ==============================

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

// ==============================
// DELETE BOOK
// ==============================

    public String deleteBook(Long id) {

        Book book = getBookById(id);

        bookRepository.delete(book);

        return "Book deleted successfully";
    }

    public Book getBookByAccession(String accessionNumber) {

        Book book = bookRepository.findByAccessionNumber(accessionNumber);

        if (book == null) {
            throw new RuntimeException("Book not found");
        }

        return book;

    }


}
