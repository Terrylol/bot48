package com.cyber48.backend.dto;

public record AdminUserDto(
        Long id,
        String username,
        String role,
        String avatarUrl,
        String personaSummary
) {
}
