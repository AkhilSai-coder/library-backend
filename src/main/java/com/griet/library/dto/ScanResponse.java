package com.griet.library.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScanResponse(
        String message,
        String type,
        String collegeId,
        Integer year,
        String branch,
        String section,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        Long durationMinutes
) {}
