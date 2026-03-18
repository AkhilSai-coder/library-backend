package com.griet.library.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "library_logs",
    indexes = {
        @Index(name = "idx_library_logs_college_id",  columnList = "college_id"),
        @Index(name = "idx_library_logs_entry_time",  columnList = "entry_time"),
        @Index(name = "idx_library_logs_active_entry", columnList = "college_id, exit_time")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** College roll number / barcode printed on student ID card */
    @Column(name = "college_id", nullable = false, length = 50)
    private String collegeId;

    /** Set automatically on insert */
    @Column(name = "entry_time", nullable = false, updatable = false)
    private LocalDateTime entryTime;

    /** NULL while student is still inside; set on exit scan */
    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    // ── Derived helper (not persisted) ────────────────────────────────────────

    /**
     * Returns total minutes spent inside the library.
     * Only meaningful when exitTime is not null.
     */
    @Transient
    public Long getDurationMinutes() {
        if (entryTime == null || exitTime == null) return null;
        return java.time.Duration.between(entryTime, exitTime).toMinutes();
    }
}
