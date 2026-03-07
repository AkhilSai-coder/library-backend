package com.griet.library.controller;

import com.griet.library.dto.BookDTO;
import com.griet.library.model.Book;
import com.griet.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://griet-central-library.vercel.app")
public class BookController {

    private final BookService bookService;

    @PostMapping("/admin/add-book")
    public Book addBook(@RequestBody BookDTO dto){
        return bookService.addBook(dto);
    }

    @GetMapping
    public List<Book> getAllBooks(){
        return bookService.getAllBooks();
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String title){
        return bookService.searchBooks(title);
    }

    @GetMapping("/accession/{number}")
    public Book getBookByAccession(@PathVariable String number){
        return bookService.getBookByAccession(number);
    }
}