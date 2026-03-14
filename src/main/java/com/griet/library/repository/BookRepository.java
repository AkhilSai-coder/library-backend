package com.griet.library.repository;

import com.griet.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * BookRepository – optimised for 130k+ records.
 *
 * Key design decisions:
 *  1. All list-returning methods return Page<Book> to prevent OOM on large datasets.
 *  2. Projection query (BookSummary) is used for catalogue listing – avoids
 *     loading heavy fields (billDate, price, source, etc.) in list views.
 *  3. COUNT queries use indexed columns only.
 *  4. Search uses ILIKE (PostgreSQL) via JPQL lower() for index-friendliness.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    // ── Paginated catalogue with optional filters ─────────────────────────────

    /**
     * Main catalogue endpoint – supports all filter combinations.
     * Null parameters are treated as "no filter" (SQL: param IS NULL OR col = param).
     * The index on (branch, category) covers the most common combined filter.
     */
    @Query("""
        SELECT b FROM Book b
        WHERE (:branch   IS NULL OR b.branch   = :branch)
          AND (:category IS NULL OR b.category = :category)
          AND (:available IS NULL OR b.available = :available)
        ORDER BY b.title ASC
        """)
    Page<Book> findWithFilters(
            @Param("branch")    String branch,
            @Param("category")  String category,
            @Param("available") Boolean available,
            Pageable pageable
    );

    /**
     * Full-text title search – paginated.
     * For 130k records, consider adding a PostgreSQL GIN/trigram index:
     *   CREATE INDEX idx_books_title_trgm ON books USING gin (title gin_trgm_ops);
     */
    @Query("""
        SELECT b FROM Book b
        WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))
           OR LOWER(b.authors) LIKE LOWER(CONCAT('%', :title, '%'))
        ORDER BY b.title ASC
        """)
    Page<Book> searchByTitleOrAuthor(@Param("title") String title, Pageable pageable);

    /**
     * Combined search + filter (power search for catalogue page).
     */
    @Query("""
        SELECT b FROM Book b
        WHERE (:query IS NULL OR
               LOWER(b.title)   LIKE LOWER(CONCAT('%', :query, '%')) OR
               LOWER(b.authors) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:branch   IS NULL OR b.branch   = :branch)
          AND (:category IS NULL OR b.category = :category)
          AND (:available IS NULL OR b.available = :available)
        ORDER BY b.title ASC
        """)
    Page<Book> searchWithFilters(
            @Param("query")     String query,
            @Param("branch")    String branch,
            @Param("category")  String category,
            @Param("available") Boolean available,
            Pageable pageable
    );

    // ── Lookup by unique identifiers ──────────────────────────────────────────

    Optional<Book> findByAccessionNumber(String accessionNumber);

    Optional<Book> findByIsbn(String isbn);

    // ── Distinct value queries (for filter dropdowns) ─────────────────────────

    @Query("SELECT DISTINCT b.branch FROM Book b WHERE b.branch IS NOT NULL ORDER BY b.branch")
    List<String> findDistinctBranches();

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findDistinctCategories();

    // ── Count queries (dashboard – fast, index-only) ──────────────────────────

    long countByAvailableTrue();
    long countByAvailableFalse();

    // ── Legacy list methods (kept for backward compatibility, internal use only) ─

    List<Book> findByBranch(String branch);
    List<Book> findByRecommendedYear(int year);
    List<Book> findByCategoryIn(List<String> categories);
}
