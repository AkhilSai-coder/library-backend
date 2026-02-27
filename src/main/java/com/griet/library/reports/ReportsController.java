package com.griet.library.reports;

import com.griet.library.model.Borrow;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return reportsService.getSystemStats();
    }

    @GetMapping("/overdue")
    public List<Borrow> getOverdueBooks() {
        return reportsService.getOverdueBooks();
    }

    @GetMapping("/fines")
    public Map<String, Object> getFineReport() {
        return reportsService.getFineReport();
    }

    @GetMapping("/most-borrowed")
    public List<Map<String, Object>> getMostBorrowedBooks() {
        return reportsService.getMostBorrowedBooks();
    }

    @GetMapping("/borrow-trend")
    public List<Map<String, Object>> getBorrowTrend() {
        return reportsService.getBorrowTrend();
    }
}