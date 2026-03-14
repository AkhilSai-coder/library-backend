package com.griet.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Book entity – optimized with composite and single-column indexes.
 *
 * Index strategy for 130k records:
 *  idx_books_title_fts  → GIN / trigram index (created manually in migration)
 *  idx_books_branch     → equality filter
 *  idx_books_category   → equality filter
 *  idx_books_available  → partial index – WHERE available = true
 *  idx_books_branch_cat → composite: branch + category (most common filter combo)
 */
@Entity
@Table(
    name = "books",
    indexes = {
        @Index(name = "idx_books_title",        columnList = "title"),
        @Index(name = "idx_books_branch",       columnList = "branch"),
        @Index(name = "idx_books_category",     columnList = "category"),
        @Index(name = "idx_books_available",    columnList = "available"),
        @Index(name = "idx_books_branch_cat",   columnList = "branch, category"),
        @Index(name = "idx_books_accession",    columnList = "accessionNumber"),
        @Index(name = "idx_books_isbn",         columnList = "isbn")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accessionNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String authors;

    private String publisher;
    private String placeOfPublication;
    private int year;

    @Column(unique = true)
    private String isbn;

    private int pages;
    private String source;
    private BigDecimal price;
    private String billNo;
    private LocalDate billDate;
    private String type;
    private String category;
    private String branch;
    private int recommendedYear;

    @Column(nullable = false)
    @Builder.Default
    private boolean available = true;
}
