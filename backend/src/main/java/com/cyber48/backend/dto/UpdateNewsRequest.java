package com.cyber48.backend.dto;

public record UpdateNewsRequest(
    String title,
    String summary,
    String sourceUrl,
    String category
) {}
