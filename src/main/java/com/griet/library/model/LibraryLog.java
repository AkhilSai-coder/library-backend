package com.griet.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "library_logs",
    indexes = {
        @Index(name = "idx_library_logs_college_id",   columnList = "college_id"),
        @Index(name = "idx_library_logs_entry_time",   columnList = "entry_time"),
        @Index(name = "idx_library_logs_active_entry", columnList = "college_id, exit_time"),
        @Index(name = "idx_library_logs_year_branch",  columnList = "year, branch")
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

    @Column(name = "college_id", nullable = false, length = 50)
    private String collegeId;

    /** 1 / 2 / 3 / 4 */
    @Column(name = "year")
    private Integer year;

    /** CSE, ECE, MECH … */
    @Column(name = "branch", length = 20)
    private String branch;

    /** A / B / C … */
    @Column(name = "section", length = 5)
    private String section;

    @Column(name = "entry_time", nullable = false, updatable = false)
    private LocalDateTime entryTime;

    /** NULL while student is still inside */
    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Transient
    public Long getDurationMinutes() {
        if (entryTime == null || exitTime == null) return null;
        return java.time.Duration.between(entryTime, exitTime).toMinutes();
    }
}