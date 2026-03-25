package com.griet.library.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class BookDTO {

    private String accessionNumber;
    private String title;
    private String authors;
    private String publisher;
    private String placeOfPublication;
    private Integer year;           // was int
    private String isbn;
    private Integer pages;          // was int
    private String source;
    private Double price;           // was double
    private String billNo;
    private LocalDate billDate;
    private String type;

    // recommendation fields
    private String category;
    private String branch;
    private Integer recommendedYear; // was int

}