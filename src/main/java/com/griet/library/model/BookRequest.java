package com.griet.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * BookRequest – tracks student/faculty borrow requests.
 *
 * Indexes:
 *  idx_request_status      → librarian pending-queue filter (most common query)
 *  idx_request_user_status → student "my requests" with status filter
 */
@Entity
@Table(
    name = "book_requests",
    indexes = {
        @Index(name = "idx_request_status",      columnList = "status"),
        @Index(name = "idx_request_user",        columnList = "user_id"),
        @Index(name = "idx_request_user_status", columnList = "user_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    /** Optional librarian note shown to student on rejection */
    private String rejectionReason;
}
