package com.griet.library.repository;

import com.griet.library.model.Book;
import com.griet.library.model.Borrow;
import com.griet.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    // active books for user
    List<Borrow> findByUserAndReturnedFalse(User user);

    // all borrowed books
    List<Borrow> findByReturnedFalse();

    // overdue books
    List<Borrow> findByReturnedFalseAndDueDateBefore(LocalDate date);

    // prevent duplicate borrow
    boolean existsByUserAndBookAndReturnedFalse(User user, Book book);

    // fine collected
    @Query("SELECT SUM(b.fine) FROM Borrow b WHERE b.returned = true")
    Double getTotalCollectedFine();

    // pending fine
    @Query("SELECT SUM(b.fine) FROM Borrow b WHERE b.returned = false")
    Double getTotalPendingFine();

    // most borrowed books
    @Query("""
        SELECT b.book.title, COUNT(b)
        FROM Borrow b
        GROUP BY b.book.title
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> getMostBorrowedBooks();

    // monthly borrow analytics
    @Query("""
        SELECT FUNCTION('TO_CHAR', b.issueDate, 'YYYY-MM'), COUNT(b)
        FROM Borrow b
        GROUP BY FUNCTION('TO_CHAR', b.issueDate, 'YYYY-MM')
        ORDER BY FUNCTION('TO_CHAR', b.issueDate, 'YYYY-MM')
    """)
    List<Object[]> getMonthlyBorrowTrend();

    // user's borrow history
    List<Borrow> findByUser(User user);

    // returned books
    List<Borrow> findByUserAndReturnedTrue(User user);

    // total borrowed count
    long countByReturnedFalse();

    // duplicate borrow check
    boolean existsByUserIdAndBookIdAndReturnedFalse(Long userId, Long bookId);

    // overdue count
    long countByDueDateBeforeAndReturnedFalse(LocalDate date);

    long countByUserAndReturnedFalse(User user);

    long countByUserAndReturnedTrue(User user);
}