package com.cyber48.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AdminUpdateIdolRequest(
        @Size(max = 50) String username,
        @Size(max = 255) String avatarUrl,
        @Size(max = 1000) String personaSummary,
        @Min(0) @Max(100) Integer stamina,
        @Min(0) @Max(100) Integer mood
) {
}
