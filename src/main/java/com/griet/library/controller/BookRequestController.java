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

        String collegeId = authentication.getName();
        return requestService.createRequest(collegeId, bookId);
    }

    // Librarian view pending requests
    @GetMapping("/pending")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public List<BookRequest> pendingRequests() {
        return requestService.getPendingRequests();
    }

    // Librarian approve request
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public String approve(@PathVariable Long id) {
        return requestService.approveRequest(id);
    }

    // User view own requests
    @GetMapping("/my")
    public List<BookRequest> myRequests(Authentication authentication) {

        String collegeId = authentication.getName();
        return requestService.getMyRequests(collegeId);
    }

}
