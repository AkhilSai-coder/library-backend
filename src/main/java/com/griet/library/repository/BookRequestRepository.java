package com.griet.library.repository;

import com.griet.library.model.BookRequest;
import com.griet.library.model.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BookRequestRepository – optimised with bulk-update queries.
 *
 * The REJECT fix uses a @Modifying + @Query to issue a single
 * UPDATE statement instead of load → mutate → save (which can
 * silently fail if the entity is detached or dirty-checking is missed).
 */
public interface BookRequestRepository extends JpaRepository<BookRequest, Long> {

    // ── Status-based queries (index: idx_request_status) ─────────────────────

    List<BookRequest> findByStatus(RequestStatus status);

    Page<BookRequest> findByStatus(RequestStatus status, Pageable pageable);

    long countByStatus(RequestStatus status);

    // ── User-scoped queries (index: idx_request_user_status) ─────────────────

    List<BookRequest> findByUser_CollegeId(String collegeId);

    List<BookRequest> findByUser_CollegeIdAndStatus(String collegeId, RequestStatus status);

    // ── Duplicate request guard ───────────────────────────────────────────────

    @Query("""
        SELECT COUNT(r) > 0 FROM BookRequest r
        WHERE r.user.collegeId = :collegeId
          AND r.book.id = :bookId
          AND r.status = 'PENDING'
        """)
    boolean existsPendingRequest(@Param("collegeId") String collegeId,
                                  @Param("bookId") Long bookId);

    // ── Bulk status updates (atomic – bypasses dirty-check issues) ───────────

    /**
     * ✅ FIX: Direct UPDATE avoids the "reject sometimes doesn't save" bug.
     * Using @Modifying ensures the update is flushed immediately within
     * the transaction boundary, regardless of the entity's persistence state.
     */
    @Modifying
    @Query("""
        UPDATE BookRequest r
        SET r.status = :status,
            r.processedAt = :processedAt,
            r.rejectionReason = :reason
        WHERE r.id = :id
        """)
    int updateRequestStatus(@Param("id")          Long id,
                             @Param("status")      RequestStatus status,
                             @Param("processedAt") LocalDateTime processedAt,
                             @Param("reason")      String reason);

    // ── Eager-fetch variant for librarian view (avoids N+1) ──────────────────

    @Query("""
        SELECT r FROM BookRequest r
        JOIN FETCH r.user u
        JOIN FETCH r.book b
        WHERE r.status = :status
        ORDER BY r.requestedAt ASC
        """)
    List<BookRequest> findPendingWithDetails(@Param("status") RequestStatus status);
}
