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

@RestController
@RequiredArgsConstructor
public class LibraryLogController {

    private final LibraryLogService libraryLogService;

    // ══════════════════════════════════════════════════════════════
    //  POST /api/scan?collegeId=XXX&mode=ENTRY&year=2&branch=CSE&section=A
    // ══════════════════════════════════════════════════════════════

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping("/api/scan")
    public ResponseEntity<?> scan(
            @RequestParam String collegeId,
            @RequestParam(defaultValue = "ENTRY") String mode,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String section
    ) {
        try {
            ScanResponse response = libraryLogService.processScan(collegeId, mode, year, branch, section);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  GET /api/logs/today
    // ══════════════════════════════════════════════════════════════

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/api/logs/today")
    public List<LibraryLog> todaysLogs() {
        return libraryLogService.getTodaysLogs();
    }

    // ══════════════════════════════════════════════════════════════
    //  GET /api/logs/student/{collegeId}
    // ══════════════════════════════════════════════════════════════

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/api/logs/student/{collegeId}")
    public ResponseEntity<?> studentHistory(@PathVariable String collegeId) {
        try {
            List<LibraryLog> logs = libraryLogService.getStudentHistory(collegeId);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
