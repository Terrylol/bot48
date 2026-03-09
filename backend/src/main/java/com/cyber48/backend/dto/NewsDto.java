package com.cyber48.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NewsDto(
    Long id,
    String title,
    String summary,
    String sourceUrl,
    String category,
    LocalDate publishedDate,
    LocalDateTime createdAt
) {}
