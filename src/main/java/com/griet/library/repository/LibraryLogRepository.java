package com.griet.library.repository;

import com.griet.library.model.LibraryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibraryLogRepository extends JpaRepository<LibraryLog, Long> {

    // ── Core business query ────────────────────────────────────────────────────

    /**
     * Finds the single open (un-exited) entry for a student.
     * Uses the composite index on (college_id, exit_time) for O(log n) lookup.
     * LIMIT 1 is safe because the service layer prevents duplicate open entries.
     */
    @Query("""
            SELECT l FROM LibraryLog l
             WHERE l.collegeId = :collegeId
               AND l.exitTime IS NULL
             ORDER BY l.entryTime DESC
             LIMIT 1
           """)
    Optional<LibraryLog> findActiveEntry(@Param("collegeId") String collegeId);

    // ── Reporting queries ──────────────────────────────────────────────────────

    /**
     * All logs whose entry_time falls within today (midnight → now).
     * Leverages the entry_time index for an efficient range scan.
     */
    @Query("""
            SELECT l FROM LibraryLog l
             WHERE l.entryTime >= :startOfDay
               AND l.entryTime < :endOfDay
             ORDER BY l.entryTime DESC
           """)
    List<LibraryLog> findTodaysLogs(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay")   LocalDateTime endOfDay
    );

    /**
     * Full history for a student, most recent first.
     * Leverages the college_id index.
     */
    List<LibraryLog> findByCollegeIdOrderByEntryTimeDesc(String collegeId);
}
