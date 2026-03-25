package com.griet.library.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.griet.library.dto.BookDTO;
import com.griet.library.model.Book;
import com.griet.library.repository.BookRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * BookService – production-optimised for 130k+ records.
 *
 * Performance highlights:
 *  • getAllBooks() is REMOVED from public API → always use paginated version
 *  • Paginated catalogue uses index-backed queries
 *  • Cache on getCatalogue / searchBooks to absorb repeated identical requests
 *  • Cache is evicted on book add/delete to prevent stale data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    private static final int MAX_PAGE_SIZE = 50; // hard ceiling per request

    // ==============================
    // ADD BOOK (ADMIN) – evicts catalogue cache
    // ==============================

    @Transactional
@CacheEvict(cacheNames = {"books-page", "books-search", "dashboard"}, allEntries = true)
public Book addBook(BookDTO dto) {

    Book book = Book.builder()
            .accessionNumber(dto.getAccessionNumber())
            .title(dto.getTitle())
            .authors(dto.getAuthors())
            .publisher(dto.getPublisher())
            .placeOfPublication(dto.getPlaceOfPublication())
            .year(dto.getYear()  != null ? dto.getYear()  : 0)   // null-safe
            .isbn(dto.getIsbn())
            .pages(dto.getPages() != null ? dto.getPages() : 0)  // null-safe
            .source(dto.getSource())
            .price(dto.getPrice() != null
                    ? BigDecimal.valueOf(dto.getPrice())
                    : BigDecimal.ZERO)                            // null-safe
            .billNo(dto.getBillNo())
            .billDate(dto.getBillDate())
            .type(dto.getType())
            .category(dto.getCategory())
            .branch(dto.getBranch())
            .recommendedYear(dto.getRecommendedYear() != null
                    ? dto.getRecommendedYear() : 0)              // null-safe
            .available(true)
            .build();

    return bookRepository.save(book);
}
    // ==============================
    // PAGINATED CATALOGUE  ← PRIMARY endpoint for frontend
    // GET /books?page=0&size=20&branch=CSE&category=Programming&available=true
    // ==============================

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = "books-page",
        key = "#page + '-' + #size + '-' + #branch + '-' + #category + '-' + #available"
    )
    public Page<Book> getCatalogue(int page, int size,
                                    String branch, String category,
                                    Boolean available) {

        // Clamp page size to prevent abuse
        int safeSize = Math.min(size, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("title").ascending());

        // If no filters → use default pageable (fastest path)
        if (branch == null && category == null && available == null) {
            return bookRepository.findAll(pageable);
        }

        return bookRepository.findWithFilters(branch, category, available, pageable);
    }

    // ==============================
    // SEARCH  (title + author, paginated)
    // GET /books/search?q=java&page=0&size=20
    // ==============================

    @Transactional(readOnly = true)
    @Cacheable(
        cacheNames = "books-search",
        key = "#query + '-' + #branch + '-' + #category + '-' + #page + '-' + #size"
    )
    public Page<Book> searchBooks(String query, String branch,
                                   String category, int page, int size) {

        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by("title").ascending());

        return bookRepository.searchWithFilters(query, branch, category, null, pageable);
    }

    // ==============================
    // SINGLE BOOK DETAIL (cached per book)
    // ==============================

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "book-detail", key = "#id")
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "book-detail", key = "'acc-' + #accessionNumber")
    public Book getBookByAccession(String accessionNumber) {
        return bookRepository.findByAccessionNumber(accessionNumber)
                .orElseThrow(() -> new RuntimeException("Book not found: " + accessionNumber));
    }

    // ==============================
    // FILTER DROPDOWN VALUES (cached, rarely changes)
    // ==============================

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "books-page", key = "'branches'")
    public List<String> getDistinctBranches() {
        return bookRepository.findDistinctBranches();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "books-page", key = "'categories'")
    public List<String> getDistinctCategories() {
        return bookRepository.findDistinctCategories();
    }

    // ==============================
    // DELETE BOOK (ADMIN)
    // ==============================

    @Transactional
    @CacheEvict(cacheNames = {"books-page", "books-search", "book-detail", "dashboard"}, allEntries = true)
    public String deleteBook(Long id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
        log.info("Book deleted: id={}, title={}", id, book.getTitle());
        return "Book deleted successfully";
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Legacy compatibility – kept so existing endpoints don't break
    // These are NOT cached because they bypass pagination safety.
    // They should only be called for small internal operations.
    // ──────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        // ⚠️ WARNING: Do NOT call this from frontend for 130k dataset.
        // Use getCatalogue() instead.  This remains for backward compatibility
        // with the issue-return librarian flow that looks up ALL borrows.
        log.warn("getAllBooks() called – consider switching to paginated endpoint");
        return bookRepository.findAll();
    }
}
