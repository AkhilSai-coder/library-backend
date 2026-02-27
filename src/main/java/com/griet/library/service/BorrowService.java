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
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /* ==============================
       BORROW BOOK (called on approve)
    ============================== */
    public Borrow borrowBook(String email, Long bookId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Prevent duplicate borrow
        boolean alreadyBorrowed =
                borrowRepository.existsByUserAndBookAndReturnedFalse(user, book);

        if (alreadyBorrowed) {
            throw new RuntimeException("Book already borrowed");
        }

        List<Borrow> activeBorrows =
                borrowRepository.findByUserAndReturnedFalse(user);

        int maxLimit;
        int dueDays;

        if (user.getRole() == Role.STUDENT) {
            maxLimit = 3;
            dueDays = 7;
        } else if (user.getRole() == Role.FACULTY) {
            maxLimit = 10;
            dueDays = 30;
        } else {
            throw new RuntimeException("Librarian cannot borrow books");
        }

        if (activeBorrows.size() >= maxLimit) {
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

        return borrowRepository.save(borrow);
    }

    /* ==============================
       RETURN BOOK
    ============================== */
    public String returnBook(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow record not found"));

        if (borrow.isReturned()) {
            return "Book already returned";
        }

        LocalDate today = LocalDate.now();

        if (today.isAfter(borrow.getDueDate())) {

            long overdueDays =
                    java.time.temporal.ChronoUnit.DAYS
                            .between(borrow.getDueDate(), today);

            double fineAmount = overdueDays * 10; // ₹10 per day
            borrow.setFine(fineAmount);
        }

        borrow.setReturned(true);
        borrowRepository.save(borrow);

        return "Book returned successfully";
    }

    /* ==============================
       GET MY BOOKS (FIXED)
    ============================== */
    public List<Borrow> getMyBooks(String email) {

        System.out.println("EMAIL FROM JWT: " + email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return borrowRepository.findByUser(user);
    }

    /* ==============================
       GET ALL BORROWS (Librarian)
    ============================== */
    public List<Borrow> getAllBorrows() {
        return borrowRepository.findAll();
    }

    public void issueBook(Long bookId, String email) {
        borrowBook(email, bookId);
    }
}