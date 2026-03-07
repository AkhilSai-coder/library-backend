package com.griet.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "books")
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

    private boolean available = true;
}