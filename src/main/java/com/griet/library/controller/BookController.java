package com.griet.library.controller;

import com.griet.library.dto.BookDTO;
import com.griet.library.model.Book;
import com.griet.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookController – all list endpoints are paginated.
 *
 * Frontend compatibility:
 *  GET /books                → was List<Book>, now Page<Book>
 *                              Frontend must read response.content[] instead of response[]
 *                              totalElements, totalPages, number also available
 *
 *  GET /books/search?title=  → legacy; still works, now paginated
 *  GET /books/accession/{n}  → unchanged
 *
 * New endpoints:
 *  GET /books/filters        → returns {branches:[], categories:[]} for dropdowns
 *  GET /books/catalogue      → paginated + filterable (preferred over /books)
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://griet-central-library.vercel.app")
public class BookController {

    private final BookService bookService;

    // ── ADMIN ─────────────────────────────────────────────────────────────────

    @PostMapping("/admin/add-book")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Book> addBook(@RequestBody BookDTO dto) {
        return ResponseEntity.ok(bookService.addBook(dto));
    }

    @DeleteMapping("/admin/delete/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.deleteBook(id));
    }

    // ── CATALOGUE (primary paginated endpoint) ────────────────────────────────

    /**
     * Primary catalogue endpoint.
     * Replaces the old GET /books that returned all books at once.
     *
     * Query params:
     *  page      (0-indexed, default 0)
     *  size      (default 20, max 50)
     *  branch    (optional filter)
     *  category  (optional filter)
     *  available (true/false, optional)
     *  q         (search query across title+author, optional)
     */
    @GetMapping
    public ResponseEntity<Page<Book>> getCatalogue(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String branch,
            @RequestParam(required = false)    String category,
            @RequestParam(required = false)    Boolean available,
            @RequestParam(required = false)    String q
    ) {
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(
                bookService.searchBooks(q, branch, category, page, size)
            );
        }
        return ResponseEntity.ok(
            bookService.getCatalogue(page, size, branch, category, available)
        );
    }

    /**
     * Legacy search endpoint – kept for backward compatibility.
     * New frontend should use GET /books?q=... instead.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam String title,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(bookService.searchBooks(title, null, null, page, size));
    }

    // ── SINGLE BOOK ───────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @GetMapping("/accession/{number}")
    public ResponseEntity<Book> getBookByAccession(@PathVariable String number) {
        return ResponseEntity.ok(bookService.getBookByAccession(number));
    }

    // ── FILTER DROPDOWN DATA ──────────────────────────────────────────────────

    /**
     * Returns all distinct branch and category values.
     * Used by frontend to populate filter dropdowns.
     * Single call replaces two separate queries.
     */
    @GetMapping("/filters")
    public ResponseEntity<BookFiltersResponse> getFilters() {
        return ResponseEntity.ok(new BookFiltersResponse(
                bookService.getDistinctBranches(),
                bookService.getDistinctCategories()
        ));
    }

    // ── Inner DTO (no separate file needed) ───────────────────────────────────

    public record BookFiltersResponse(
            List<String> branches,
            List<String> categories
    ) {}
}
