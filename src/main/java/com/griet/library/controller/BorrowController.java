package com.griet.library.controller;

import com.griet.library.model.Borrow;
import com.griet.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT','FACULTY')")
    public List<Borrow> myBorrows(Authentication authentication) {
        return borrowService.getMyBooks(authentication.getName());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public List<Borrow> allBorrows() {
        return borrowService.getAllBorrows();
    }

    @PostMapping("/return/{borrowId}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String returnBook(@PathVariable Long borrowId) {
        return borrowService.returnBook(borrowId);
    }

    @PostMapping("/issue/{bookId}/{email}")
    public ResponseEntity<?> issueBook(
            @PathVariable Long bookId,
            @PathVariable String email) {

        borrowService.issueBook(bookId, email);
        return ResponseEntity.ok("Issued");
    }
}