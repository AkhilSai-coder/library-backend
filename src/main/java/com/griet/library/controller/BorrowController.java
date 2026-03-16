package com.griet.library.controller;

import com.griet.library.model.Borrow;
import com.griet.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /* ==============================
       STUDENT / FACULTY VIEW MY BOOKS
       ============================== */

    @PreAuthorize("hasAnyRole('STUDENT','FACULTY')")
    @GetMapping("/my-books")
    public List<Borrow> myBorrows(Authentication authentication) {
        return borrowService.getMyBooks(authentication.getName());
    }

    /* ==============================
       LIBRARIAN VIEW ALL BORROWS
       ============================== */

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/all")
    public List<Borrow> allBorrows() {
        return borrowService.getAllBorrows();
    }

    /* ==============================
       RETURN BOOK
       ============================== */

    @PreAuthorize("hasRole('LIBRARIAN')")
@PostMapping("/return/{borrowId}")
public String returnBook(@PathVariable Long borrowId) {
        return borrowService.returnBook(borrowId);
    }

    /* ==============================
       ISSUE BOOK (LIBRARIAN)
       ============================== */

    @PostMapping("/issue")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void issueBook(@RequestParam Long bookId,
                          @RequestParam String collegeId) {

        borrowService.issueBook(bookId, collegeId);
    }
}