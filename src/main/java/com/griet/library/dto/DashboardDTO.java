package com.griet.library.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DashboardDTO – single-call dashboard payload.
 *
 * Replaces 4 separate admin API calls (/total-books, /total-users,
 * /borrowed-books, /overdue-books) with one cached call to /admin/dashboard.
 *
 * Added fields:
 *  - availableBooks    : total books currently on shelf
 *  - totalStudents     : student count
 *  - totalFaculty      : faculty count
 *  - pendingRequests   : borrow request queue size (librarian badge)
 *  - totalFineCollected: fine revenue (reports widget)
 */
@Data
@Builder
public class DashboardDTO {

    // Book stats
    private long totalBooks;
    private long availableBooks;
    private long borrowedBooks;
    private long overdueBooks;

    // User stats
    private long totalUsers;
    private long totalStudents;
    private long totalFaculty;

    // Request stats
    private long pendingRequests;

    // Fine stats
    private double totalFineCollected;
}
