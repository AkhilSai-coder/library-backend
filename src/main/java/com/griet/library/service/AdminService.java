package com.griet.library.service;

import com.griet.library.repository.BookRepository;
import com.griet.library.repository.BorrowRepository;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;

    // TOTAL BOOKS IN LIBRARY
    public long totalBooks() {
        return bookRepository.count();
    }

    // TOTAL REGISTERED USERS
    public long totalUsers() {
        return userRepository.count();
    }

    // CURRENTLY BORROWED BOOKS
    public long borrowedBooks() {
        return borrowRepository.countByReturnedFalse();
    }

    // OVERDUE BOOKS
    public long overdueBooks() {
        return borrowRepository.countByDueDateBeforeAndReturnedFalse(LocalDate.now());
    }

}
