package com.griet.library.repository;

import com.griet.library.model.Book;
import com.griet.library.model.Borrow;
import com.griet.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    List<Borrow> findByUserAndReturnedFalse(User user);

    List<Borrow> findByReturnedFalse();

    List<Borrow> findByReturnedFalseAndDueDateBefore(LocalDate date);

    boolean existsByUserAndBookAndReturnedFalse(User user, Book book);

    @Query("SELECT SUM(b.fine) FROM Borrow b WHERE b.returned = true")
    Double getTotalCollectedFine();

    @Query("SELECT SUM(b.fine) FROM Borrow b WHERE b.returned = false")
    Double getTotalPendingFine();

    @Query("""
        SELECT b.book.title, COUNT(b)
        FROM Borrow b
        GROUP BY b.book.title
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> getMostBorrowedBooks();

    @Query("""
        SELECT FUNCTION('TO_CHAR', b.issueDate, 'YYYY-MM'), COUNT(b)
        FROM Borrow b
        GROUP BY FUNCTION('TO_CHAR', b.issueDate, 'YYYY-MM')
        ORDER BY FUNCTION('TO_CHAR', b.issueDate, 'YYYY-MM')
    """)
    List<Object[]> getMonthlyBorrowTrend();

    List<Borrow> findByUser(User user);


    List<Borrow> findByUserAndReturnedTrue(User user);
}