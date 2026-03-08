package com.cyber48.backend.dto;

public record FanProfileDto(
        Long userId,
        String username,
        String avatarUrl,
        String personaSummary
) {
}
