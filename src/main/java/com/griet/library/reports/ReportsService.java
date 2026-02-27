package com.griet.library.reports;

import com.griet.library.model.Borrow;
import com.griet.library.repository.BookRepository;
import com.griet.library.repository.BorrowRepository;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;

    public Map<String, Object> getSystemStats() {

        long totalBooks = bookRepository.count();
        long borrowedBooks = borrowRepository.findByReturnedFalse().size();
        long availableBooks = totalBooks - borrowedBooks;
        long totalUsers = userRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", totalBooks);
        stats.put("borrowedBooks", borrowedBooks);
        stats.put("availableBooks", availableBooks);
        stats.put("totalUsers", totalUsers);

        return stats;
    }

    public Map<String, Object> getFineReport() {

        Double collected = borrowRepository.getTotalCollectedFine();
        Double pending = borrowRepository.getTotalPendingFine();

        if (collected == null) collected = 0.0;
        if (pending == null) pending = 0.0;

        Map<String, Object> report = new HashMap<>();
        report.put("totalCollectedFine", collected);
        report.put("totalPendingFine", pending);
        report.put("totalFineGenerated", collected + pending);

        return report;
    }

    public List<Borrow> getOverdueBooks() {
        return borrowRepository
                .findByReturnedFalseAndDueDateBefore(LocalDate.now());
    }

    public List<Map<String, Object>> getMostBorrowedBooks() {

        List<Object[]> results = borrowRepository.getMostBorrowedBooks();

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {

            Map<String, Object> data = new HashMap<>();
            data.put("title", row[0]);
            data.put("borrowCount", row[1]);

            response.add(data);
        }

        return response;
    }

    public List<Map<String, Object>> getBorrowTrend() {

        List<Object[]> results = borrowRepository.getMonthlyBorrowTrend();

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {

            Map<String, Object> data = new HashMap<>();
            data.put("month", row[0]);
            data.put("borrowCount", row[1]);

            response.add(data);
        }

        return response;
    }
}