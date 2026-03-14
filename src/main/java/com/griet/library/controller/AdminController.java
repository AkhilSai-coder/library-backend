package com.griet.library.controller;

import com.griet.library.dto.DashboardDTO;
import com.griet.library.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AdminController – single /dashboard endpoint replaces 4 separate calls.
 *
 * Before (4 frontend API calls on page load):
 *   GET /admin/total-books
 *   GET /admin/total-users
 *   GET /admin/borrowed-books
 *   GET /admin/overdue-books
 *
 * After (1 cached call):
 *   GET /admin/dashboard  → DashboardDTO (all stats, cached 5 min)
 *
 * Legacy endpoints are kept so existing frontend code doesn't break.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://griet-central-library.vercel.app")
public class AdminController {

    private final AdminService adminService;

    // ── PRIMARY: single dashboard call ───────────────────────────────────────

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<DashboardDTO> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    // ── LEGACY: individual stat endpoints (backward compatible) ───────────────

    @GetMapping("/total-books")
    public ResponseEntity<Long> totalBooks() {
        return ResponseEntity.ok(adminService.totalBooks());
    }

    @GetMapping("/total-users")
    public ResponseEntity<Long> totalUsers() {
        return ResponseEntity.ok(adminService.totalUsers());
    }

    @GetMapping("/borrowed-books")
    public ResponseEntity<Long> borrowedBooks() {
        return ResponseEntity.ok(adminService.borrowedBooks());
    }

    @GetMapping("/overdue-books")
    public ResponseEntity<Long> overdueBooks() {
        return ResponseEntity.ok(adminService.overdueBooks());
    }
}
