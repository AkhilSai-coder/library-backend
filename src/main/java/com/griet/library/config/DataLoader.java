package com.griet.library.config;

import com.griet.library.model.Book;
import com.griet.library.model.Role;
import com.griet.library.model.User;
import com.griet.library.repository.BookRepository;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "1234";

    @Override
    public void run(String... args) {

        // ==============================
        // CREATE DEFAULT USERS
        // ==============================

        if (userRepository.count() == 0) {

            // LIBRARIAN
            userRepository.save(User.builder()
                    .name("Librarian")
                    .collegeId("LIB001")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(Role.LIBRARIAN)
                    .build());

            // STUDENTS
            userRepository.save(User.builder()
                    .name("Student One")
                    .collegeId("22341A0501")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(Role.STUDENT)
                    .build());

            userRepository.save(User.builder()
                    .name("Student Two")
                    .collegeId("22341A0502")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(Role.STUDENT)
                    .build());

            userRepository.save(User.builder()
                    .name("Student Three")
                    .collegeId("22341A0503")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(Role.STUDENT)
                    .build());

            // FACULTY
            userRepository.save(User.builder()
                    .name("Faculty One")
                    .collegeId("FAC001")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(Role.FACULTY)
                    .build());

            userRepository.save(User.builder()
                    .name("Faculty Two")
                    .collegeId("FAC002")
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .role(Role.FACULTY)
                    .build());

            System.out.println("Default users created with password: 1234");
        }

        // ==============================
        // CREATE DEFAULT BOOKS
        // ==============================

        if (bookRepository.count() == 0) {

            for (int i = 1; i <= 50; i++) {

                Book book = Book.builder()
                        .accessionNumber("ACC" + String.format("%03d", i))
                        .title("Book Title " + i)
                        .authors("Author " + i)
                        .price(BigDecimal.valueOf(500 + i))
                        .available(true)
                        .build();

                bookRepository.save(book);
            }

            System.out.println("50 books inserted successfully!");
        }
    }

}