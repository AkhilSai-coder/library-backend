package com.griet.library.service;

import com.griet.library.model.*;
import com.griet.library.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BookRequestService – fixed and hardened.
 *
 * Root cause of "reject sometimes doesn't update":
 *  • The old code did requestRepo.save(request) after mutating a detached entity.
 *    When Spring's dirty-checking window had already closed, the save() was a no-op.
 *
 * Fix applied:
 *  • rejectRequest() uses @Modifying direct UPDATE query → guaranteed DB write.
 *  • approveRequest() is fully @Transactional and saves in the correct order.
 *  • Both methods are wrapped in transactions to prevent partial updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookRequestService {

    private final BookRequestRepository requestRepo;
    private final BookRepository bookRepo;
    private final BorrowService borrowService;
    private final UserRepository userRepository;

    // ==============================
    // STUDENT CREATE REQUEST
    // ==============================

    @Transactional
    public BookRequest createRequest(String collegeId, Long bookId) {

        User user = userRepository.findByCollegeId(collegeId)
                .orElseThrow(() -> new RuntimeException("User not found: " + collegeId));

        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        // Prevent duplicate pending requests for the same book
        if (requestRepo.existsPendingRequest(collegeId, bookId)) {
            throw new RuntimeException("You already have a pending request for this book");
        }

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is currently not available for request");
        }

        BookRequest request = BookRequest.builder()
                .user(user)
                .book(book)
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        BookRequest saved = requestRepo.save(request);
        log.info("Request created: requestId={}, user={}, book={}",
                saved.getId(), collegeId, bookId);

        return saved;
    }

    // ==============================
    // LIBRARIAN VIEW PENDING REQUESTS (with user+book eagerly loaded)
    // ==============================

    @Transactional(readOnly = true)
    public List<BookRequest> getPendingRequests() {
        // Uses JOIN FETCH to avoid N+1 problem
        return requestRepo.findPendingWithDetails(RequestStatus.PENDING);
    }

    // ==============================
    // ✅ FIX: LIBRARIAN APPROVE REQUEST
    // Fully transactional – borrow creation and status update in same TX
    // ==============================

    @Transactional
    public String approveRequest(Long requestId) {

        // Load inside transaction – entity is MANAGED, dirty-check will work
        BookRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            return "Request already processed (status=" + request.getStatus() + ")";
        }

        // Create the borrow record first (may throw if book unavailable)
        borrowService.borrowBook(
                request.getUser().getCollegeId(),
                request.getBook().getId()
        );

        // ✅ Use direct UPDATE query – guaranteed write, no dirty-check dependency
        int updated = requestRepo.updateRequestStatus(
                requestId,
                RequestStatus.APPROVED,
                LocalDateTime.now(),
                null
        );

        if (updated == 0) {
            throw new RuntimeException("Failed to update request status – DB write rejected");
        }

        log.info("Request APPROVED: requestId={}", requestId);
        return "Request approved successfully";
    }

    // ==============================
    // ✅ FIX: LIBRARIAN REJECT REQUEST
    // Root cause was: detached entity + save() = silent no-op
    // Fix: @Modifying UPDATE query → atomic, guaranteed DB write
    // ==============================

    @Transactional
    public String rejectRequest(Long requestId, String reason) {

        BookRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            return "Request already processed (status=" + request.getStatus() + ")";
        }

        // ✅ Direct UPDATE – cannot silently fail due to dirty-check issues
        int updated = requestRepo.updateRequestStatus(
                requestId,
                RequestStatus.REJECTED,
                LocalDateTime.now(),
                reason
        );

        if (updated == 0) {
            // This should never happen, but if it does, throw so TX is rolled back
            throw new RuntimeException("REJECT UPDATE FAILED for requestId=" + requestId
                    + " – no rows affected. Check DB constraints.");
        }

        log.info("Request REJECTED: requestId={}, reason={}", requestId, reason);
        return "Request rejected successfully";
    }

    // ==============================
    // STUDENT VIEW OWN REQUESTS
    // ==============================

    @Transactional(readOnly = true)
    public List<BookRequest> getMyRequests(String collegeId) {
        return requestRepo.findByUser_CollegeId(collegeId);
    }

    // ==============================
    // PENDING COUNT (for dashboard badge)
    // ==============================

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return requestRepo.countByStatus(RequestStatus.PENDING);
    }
}
