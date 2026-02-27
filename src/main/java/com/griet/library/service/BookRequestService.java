package com.griet.library.service;

import com.griet.library.model.*;
import com.griet.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookRequestService {

    private final BookRequestRepository requestRepo;
    private final BookRepository bookRepo;
    private final BorrowService borrowService;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;

    // ✅ Student creates request
    public BookRequest createRequest(String email, Long bookId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        BookRequest request = BookRequest.builder()
                .user(user)
                .book(book)
                .status(RequestStatus.PENDING)
                .build();

        return requestRepo.save(request);
    }

    // ✅ Librarian get pending requests
    public List<BookRequest> getPendingRequests() {
        return requestRepo.findByStatus(RequestStatus.PENDING);
    }

    // ✅ Librarian approve request
    public String approveRequest(Long requestId) {

        BookRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            return "Already processed";
        }

        // Create borrow record
        borrowService.borrowBook(
                request.getUser().getEmail(),
                request.getBook().getId()
        );

        request.setStatus(RequestStatus.APPROVED);
        requestRepo.save(request);

        return "Request approved successfully";
    }

    // ✅ Student get their requests
    public List<BookRequest> getMyRequests(String email) {
        return requestRepo.findByUser_Email(email);
    }

    public String returnBook(Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Borrow not found"));

        borrow.setReturned(true);
        borrowRepository.save(borrow);

        return "Book returned successfully";
    }
}