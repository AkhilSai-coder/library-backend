package com.griet.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Returned by POST /api/scan — carries enough information for the
 * front-end scanner page to display a rich confirmation card.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScanResponse(

        /** "Entry recorded" | "Exit recorded" */
        String message,

        /** ENTRY | EXIT */
        String type,

        String collegeId,

        LocalDateTime entryTime,

        /** Null while the student is still inside */
        LocalDateTime exitTime,

        /** Only present on EXIT — total minutes inside the library */
        Long durationMinutes
) {}
