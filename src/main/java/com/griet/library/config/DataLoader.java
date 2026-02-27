package com.griet.library.config;

import com.griet.library.model.Book;
import com.griet.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {

        if (bookRepository.count() == 0) {

            for (int i = 1; i <= 50; i++) {
                Book book = Book.builder()
                        .title("Book Title " + i)
                        .author("Author " + i)
                        .price(500.0 + i)
                        .build();

                bookRepository.save(book);
            }

            System.out.println("50 books inserted successfully!");
        }
    }
}