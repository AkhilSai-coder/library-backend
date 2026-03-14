package com.griet.library.controller;

import com.griet.library.model.BookRequest;
import com.griet.library.service.BookRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BookRequestController – fixed reject endpoint added.
 *
 * Changes from original:
 *  • POST /request/reject/{id}  → NEW (was missing, causing status not updating)
 *  • GET  /request/pending-count → NEW (dashboard badge)
 *  • All endpoints return ResponseEntity for consistent HTTP status codes
 */
@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://griet-central-library.vercel.app")
public class BookRequestController {

    private final BookRequestService requestService;

    // ── STUDENT ───────────────────────────────────────────────────────────────

    /** Student sends a borrow request */
    @PostMapping("/{bookId}")
    public ResponseEntity<BookRequest> requestBook(
            @PathVariable Long bookId,
            Authentication authentication
    ) {
        String collegeId = authentication.getName();
        return ResponseEntity.ok(requestService.createRequest(collegeId, bookId));
    }

    /** Student views their own requests */
    @GetMapping("/my")
    public ResponseEntity<List<BookRequest>> myRequests(Authentication authentication) {
        String collegeId = authentication.getName();
        return ResponseEntity.ok(requestService.getMyRequests(collegeId));
    }

    // ── LIBRARIAN ─────────────────────────────────────────────────────────────

    /** Librarian views all pending requests (eager-loaded, no N+1) */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<List<BookRequest>> pendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    /** Pending request count – used for dashboard notification badge */
    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, Long>> pendingCount() {
        return ResponseEntity.ok(Map.of("count", requestService.getPendingCount()));
    }

    /** Librarian approves a request → creates borrow record */
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, String>> approve(@PathVariable Long id) {
        String result = requestService.approveRequest(id);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * ✅ FIX: REJECT endpoint (was missing from original – root cause of bug).
     *
     * Body: { "reason": "Optional rejection note to student" }
     * Reason is optional – send empty string or omit if no note needed.
     */
    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Map<String, String>> reject(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequest body
    ) {
        String reason = (body != null) ? body.reason() : null;
        String result = requestService.rejectRequest(id, reason);
        return ResponseEntity.ok(Map.of("message", result));
    }

    // ── Request body record ───────────────────────────────────────────────────

    public record RejectRequest(String reason) {}
}
