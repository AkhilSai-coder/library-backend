package com.griet.library.service;

import com.griet.library.dto.DashboardDTO;
import com.griet.library.model.RequestStatus;
import com.griet.library.model.Role;
import com.griet.library.repository.BookRepository;
import com.griet.library.repository.BookRequestRepository;
import com.griet.library.repository.BorrowRepository;
import com.griet.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * AdminService – optimised dashboard aggregation.
 *
 * Key improvement:
 *  • All 4 dashboard stats are fetched in ONE cached call instead of 4 separate
 *    HTTP requests from the frontend.
 *  • @Cacheable("dashboard") means 100 concurrent users → 1 DB hit per 5 minutes.
 *  • Each count query hits an indexed column (fast even at 130k rows).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final BookRepository         bookRepository;
    private final UserRepository         userRepository;
    private final BorrowRepository       borrowRepository;
    private final BookRequestRepository  requestRepository;

    /**
     * Single cached dashboard call.
     *
     * Cache key "all" is a singleton – every user gets the same stats snapshot.
     * TTL is 5 minutes (configured in CacheConfig).
     *
     * DB queries issued (all indexed, sub-millisecond at 130k rows):
     *  1. bookRepository.count()
     *  2. bookRepository.countByAvailableTrue()
     *  3. borrowRepository.countByReturnedFalse()
     *  4. borrowRepository.countByDueDateBeforeAndReturnedFalse(today)
     *  5. userRepository.count()
     *  6. userRepository.countByRole(STUDENT)
     *  7. userRepository.countByRole(FACULTY)
     *  8. requestRepository.countByStatus(PENDING)
     *  9. borrowRepository.getTotalCollectedFine()
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "dashboard", key = "'all'")
    public DashboardDTO getDashboard() {

        log.debug("Building dashboard stats from DB (cache miss)");

        double fineCollected = 0.0;
        try {
            Double rawFine = borrowRepository.getTotalCollectedFine();
            fineCollected = rawFine != null ? rawFine : 0.0;
        } catch (Exception e) {
            log.warn("Could not fetch fine totals: {}", e.getMessage());
        }

        return DashboardDTO.builder()
                .totalBooks(bookRepository.count())
                .availableBooks(bookRepository.countByAvailableTrue())
                .borrowedBooks(borrowRepository.countByReturnedFalse())
                .overdueBooks(borrowRepository.countByDueDateBeforeAndReturnedFalse(LocalDate.now()))
                .totalUsers(userRepository.count())
                .totalStudents(userRepository.countByRole(Role.STUDENT))
                .totalFaculty(userRepository.countByRole(Role.FACULTY))
                .pendingRequests(requestRepository.countByStatus(RequestStatus.PENDING))
                .totalFineCollected(fineCollected)
                .build();
    }

    // ── Legacy individual methods (kept for backward compatibility) ───────────
    // AdminController's individual endpoints (/total-books etc.) delegate here

    @Transactional(readOnly = true)
    public long totalBooks()    { return bookRepository.count(); }

    @Transactional(readOnly = true)
    public long totalUsers()    { return userRepository.count(); }

    @Transactional(readOnly = true)
    public long borrowedBooks() { return borrowRepository.countByReturnedFalse(); }

    @Transactional(readOnly = true)
    public long overdueBooks()  {
        return borrowRepository.countByDueDateBeforeAndReturnedFalse(LocalDate.now());
    }
}
