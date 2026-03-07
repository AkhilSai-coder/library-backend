package com.griet.library.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDTO {

    private String accessionNumber;
    private String title;
    private String authors;
    private String publisher;
    private String placeOfPublication;
    private int year;
    private String isbn;
    private int pages;
    private String source;
    private double price;
    private String billNo;
    private LocalDate billDate;
    private String type;

    // recommendation fields
    private String category;
    private String branch;
    private int recommendedYear;

}
