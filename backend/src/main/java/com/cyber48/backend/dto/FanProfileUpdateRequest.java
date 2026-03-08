package com.cyber48.backend.dto;

import jakarta.validation.constraints.Size;

public record FanProfileUpdateRequest(
        @Size(max = 255) String avatarUrl,
        @Size(max = 1000) String personaSummary
) {
}
