package com.griet.library.service;

import com.griet.library.dto.ScanResponse;
import com.griet.library.model.LibraryLog;
import com.griet.library.repository.LibraryLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryLogService {

    private final LibraryLogRepository libraryLogRepository;

    // ══════════════════════════════════════════════════════════════
    //  Core scan logic
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ScanResponse processScan(String collegeId, String mode,
                                    Integer year, String branch, String section) {

        if (collegeId == null || collegeId.isBlank()) {
            throw new IllegalArgumentException("College ID must not be blank");
        }

        String normalizedId = collegeId.trim().toUpperCase();
        Optional<LibraryLog> activeEntry = libraryLogRepository.findActiveEntry(normalizedId);

        // If mode is explicitly EXIT, force exit even if called with entry
        boolean forceExit = "EXIT".equalsIgnoreCase(mode);

        if (activeEntry.isEmpty() && !forceExit) {
            return recordEntry(normalizedId, year, branch, section);
        } else if (activeEntry.isPresent()) {
            return recordExit(activeEntry.get());
        } else {
            // EXIT requested but no active entry found
            throw new IllegalStateException("No active entry found for " + normalizedId + ". Please record entry first.");
        }
    }

    // ── Private helpers ────────────────────────────────────────────

    private ScanResponse recordEntry(String collegeId, Integer year, String branch, String section) {

        // NOTE: local variable named 'entry' — NOT 'log' — to avoid conflict with @Slf4j 'log' field
        LibraryLog entry = LibraryLog.builder()
                .collegeId(collegeId)
                .year(year)
                .branch(branch)
                .section(section)
                .entryTime(LocalDateTime.now())
                .build();

        libraryLogRepository.save(entry);

        log.info("ENTRY recorded → collegeId={} year={} branch={} at {}",
                collegeId, year, branch, entry.getEntryTime());

        return new ScanResponse(
                "Entry recorded", "ENTRY",
                collegeId, year, branch, section,
                entry.getEntryTime(), null, null
        );
    }

    private ScanResponse recordExit(LibraryLog openEntry) {

        LocalDateTime exitTime = LocalDateTime.now();
        openEntry.setExitTime(exitTime);
        libraryLogRepository.save(openEntry);

        Long duration = openEntry.getDurationMinutes();

        log.info("EXIT recorded → collegeId={} at {} | duration={}min",
                openEntry.getCollegeId(), exitTime, duration);

        return new ScanResponse(
                "Exit recorded", "EXIT",
                openEntry.getCollegeId(),
                openEntry.getYear(),
                openEntry.getBranch(),
                openEntry.getSection(),
                openEntry.getEntryTime(), exitTime, duration
        );
    }

    // ══════════════════════════════════════════════════════════════
    //  Reporting
    // ══════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<LibraryLog> getTodaysLogs() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = LocalDate.now().atTime(LocalTime.MAX);
        return libraryLogRepository.findTodaysLogs(startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public List<LibraryLog> getStudentHistory(String collegeId) {
        if (collegeId == null || collegeId.isBlank()) {
            throw new IllegalArgumentException("College ID must not be blank");
        }
        return libraryLogRepository
                .findByCollegeIdOrderByEntryTimeDesc(collegeId.trim().toUpperCase());
    }
}
