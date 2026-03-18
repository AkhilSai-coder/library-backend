package com.griet.library.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.griet.library.dto.ScanResponse;
import com.griet.library.model.LibraryLog;
import com.griet.library.repository.LibraryLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryLogService {

    private final LibraryLogRepository libraryLogRepository;

    // ══════════════════════════════════════════════════════════════════════════
    //  Core scan logic
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Called on every ID-card barcode scan.
     *
     * <ul>
     *   <li>If no open entry exists  → create ENTRY record.</li>
     *   <li>If an open entry exists  → stamp EXIT time and return duration.</li>
     * </ul>
     *
     * The method is {@code @Transactional} so that the read + write happen in
     * a single atomic unit — preventing race conditions if two scan terminals
     * fire at the same millisecond for the same student.
     */
    @Transactional
    public ScanResponse processScan(String collegeId) {

        if (collegeId == null || collegeId.isBlank()) {
            throw new IllegalArgumentException("College ID must not be blank");
        }

        String normalizedId = collegeId.trim().toUpperCase();

        Optional<LibraryLog> activeEntry =
                libraryLogRepository.findActiveEntry(normalizedId);

        if (activeEntry.isEmpty()) {
            return recordEntry(normalizedId);
        } else {
            return recordExit(activeEntry.get());
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private ScanResponse recordEntry(String collegeId) {

    LibraryLog libraryLog = LibraryLog.builder()
            .collegeId(collegeId)
            .entryTime(LocalDateTime.now())
            .build();

    libraryLogRepository.save(libraryLog);

    log.info("ENTRY recorded → collegeId={} at {}", collegeId, libraryLog.getEntryTime());

    return new ScanResponse(
            "Entry recorded",
            "ENTRY",
            collegeId,
            libraryLog.getEntryTime(),
            null,
            null
    );
}

    private ScanResponse recordExit(LibraryLog openLog) {

        LocalDateTime exitTime = LocalDateTime.now();
        openLog.setExitTime(exitTime);

        libraryLogRepository.save(openLog);

        Long duration = openLog.getDurationMinutes();

        log.info("EXIT recorded  → collegeId={} at {} | duration={}min",
                openLog.getCollegeId(), exitTime, duration);

        return new ScanResponse(
                "Exit recorded",
                "EXIT",
                openLog.getCollegeId(),
                openLog.getEntryTime(),
                exitTime,
                duration
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Reporting
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all library_logs whose entry_time is today (local server date).
     */
    @Transactional(readOnly = true)
    public List<LibraryLog> getTodaysLogs() {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);

        return libraryLogRepository.findTodaysLogs(startOfDay, endOfDay);
    }

    /**
     * Returns the full entry/exit history for a specific student.
     */
    @Transactional(readOnly = true)
    public List<LibraryLog> getStudentHistory(String collegeId) {

        if (collegeId == null || collegeId.isBlank()) {
            throw new IllegalArgumentException("College ID must not be blank");
        }

        return libraryLogRepository
                .findByCollegeIdOrderByEntryTimeDesc(collegeId.trim().toUpperCase());
    }
}
