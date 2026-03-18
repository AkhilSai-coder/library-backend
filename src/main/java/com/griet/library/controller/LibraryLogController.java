package com.griet.library.controller;

import com.griet.library.dto.ScanResponse;
import com.griet.library.model.LibraryLog;
import com.griet.library.service.LibraryLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the library entry/exit tracking feature.
 *
 * Endpoints:
 *   POST /api/scan?collegeId=XXX          — barcode scan (entry or exit)
 *   GET  /api/logs/today                  — today's log list (LIBRARIAN only)
 *   GET  /api/logs/student/{collegeId}    — student history  (LIBRARIAN only)
 *
 * The scan endpoint is deliberately permitted for LIBRARIAN role only — the
 * physical barcode scanner at the library gate is operated by staff. Adjust
 * the @PreAuthorize annotation if you want kiosk/self-service access.
 */
@RestController
@RequiredArgsConstructor
public class LibraryLogController {

    private final LibraryLogService libraryLogService;

    // ══════════════════════════════════════════════════════════════════════════
    //  POST /api/scan?collegeId=XXX
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Main scan endpoint — called every time a student swipes/scans their
     * ID card at the library gate.
     *
     * <p>Returns HTTP 200 with a {@link ScanResponse} describing whether an
     * entry or exit was recorded, plus timestamp and duration details.</p>
     *
     * <p>Returns HTTP 400 if the collegeId is blank or missing.</p>
     */
    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping("/api/scan")
    public ResponseEntity<?> scan(@RequestParam String collegeId) {

        try {
            ScanResponse response = libraryLogService.processScan(collegeId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GET /api/logs/today
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all entry/exit records for today, ordered by entry_time DESC.
     * Intended for the librarian dashboard "Who is in the library right now?"
     * view.
     */
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/api/logs/today")
    public List<LibraryLog> todaysLogs() {
        return libraryLogService.getTodaysLogs();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GET /api/logs/student/{collegeId}
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns the complete visit history for a single student, most recent
     * visit first. Includes duration (minutes) for completed visits.
     */
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/api/logs/student/{collegeId}")
    public ResponseEntity<?> studentHistory(@PathVariable String collegeId) {

        try {
            List<LibraryLog> logs = libraryLogService.getStudentHistory(collegeId);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
