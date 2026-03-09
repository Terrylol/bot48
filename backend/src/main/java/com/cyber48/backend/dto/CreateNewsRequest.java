package com.cyber48.backend.dto;

public record CreateNewsRequest(
    String title,
    String summary,
    String sourceUrl,
    String category
) {}
