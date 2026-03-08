package com.cyber48.backend.dto;

public record IdolSummaryDto(
        Long id,
        String username,
        String avatarUrl,
        String personaSummary,
        Integer stamina,
        Integer mood
) {
}
