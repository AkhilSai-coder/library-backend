package com.griet.library.controller;

import com.griet.library.model.BookRequest;
import com.griet.library.service.BookRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class BookRequestController {

    private final BookRequestService requestService;

    // Student sends request
    @PostMapping("/{bookId}")
    public BookRequest requestBook(@PathVariable Long bookId,
                                   Authentication authentication) {
        String email = authentication.getName();
        return requestService.createRequest(email, bookId);
    }

    // Librarian view pending
    @GetMapping("/pending")
    public List<BookRequest> pendingRequests() {
        return requestService.getPendingRequests();
    }

    // Librarian approve
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String approve(@PathVariable Long id, Authentication authentication) {

        System.out.println("USER: " + authentication.getName());
        System.out.println("ROLES: " + authentication.getAuthorities());

        return requestService.approveRequest(id);
    }

    // ✅ ADD THIS
    @GetMapping("/my")
    public List<BookRequest> myRequests(Authentication authentication) {
        String email = authentication.getName();
        return requestService.getMyRequests(email);
    }

}