package com.griet.library.service;

import com.griet.library.model.Book;
import com.griet.library.model.Borrow;
import com.griet.library.model.Role;
import com.griet.library.model.User;
import com.griet.library.repository.BookRepository;
import com.griet.library.repository.BorrowRepository;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private static final double FINE_PER_DAY = 10;

// ==============================
// BORROW BOOK
// ==============================

    public Borrow borrowBook(String collegeId, Long bookId) {

        User user = userRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        boolean alreadyBorrowed =
                borrowRepository.existsByUserAndBookAndReturnedFalse(user, book);

        if (alreadyBorrowed) {
            throw new RuntimeException("You already borrowed this book");
        }

        if (!book.isAvailable()) {
            throw new RuntimeException("Book not available");
        }

        long activeBorrows =
                borrowRepository.countByUserAndReturnedFalse(user);

        int maxLimit;
        int dueDays;

        if (user.getRole() == Role.STUDENT) {
            maxLimit = 3;
            dueDays = 7;
        } else if (user.getRole() == Role.FACULTY) {
            maxLimit = 5;
            dueDays = 30;
        } else {
            throw new RuntimeException("Librarian cannot borrow books");
        }

        if (activeBorrows >= maxLimit) {
            throw new RuntimeException("Borrow limit exceeded");
        }

        Borrow borrow = Borrow.builder()
                .user(user)
                .book(book)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(dueDays))
                .returned(false)
                .fine(0.0)
                .build();

        book.setAvailable(false);
        bookRepository.save(book);

        return borrowRepository.save(borrow);
    }

// ==============================
// RETURN BOOK
// ==============================

    public String returnBook(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        if (borrow.isReturned()) {
            return "Book already returned";
        }

        LocalDate today = LocalDate.now();

        if (today.isAfter(borrow.getDueDate())) {

            long overdueDays =
                    ChronoUnit.DAYS.between(borrow.getDueDate(), today);

            double fine = overdueDays * FINE_PER_DAY;

            borrow.setFine(fine);
        }

        borrow.setReturned(true);

        Book book = borrow.getBook();
        book.setAvailable(true);

        bookRepository.save(book);
        borrowRepository.save(borrow);

        return "Book returned successfully";
    }

// ==============================
// USER BORROW HISTORY
// ==============================

    public List<Borrow> getMyBooks(String collegeId) {

        User user = userRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return borrowRepository.findByUser(user);
    }

// ==============================
// LIBRARIAN VIEW ALL BORROWS
// ==============================

    public List<Borrow> getAllBorrows() {
        return borrowRepository.findAll();
    }

// ==============================
// ISSUE BOOK (LIBRARIAN)
// ==============================

    public void issueBook(Long bookId, String collegeId) {
        borrowBook(collegeId, bookId);
    }

}
