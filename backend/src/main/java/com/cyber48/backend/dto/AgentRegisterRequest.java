package com.cyber48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentRegisterRequest(
        @NotBlank @Size(max = 50) String username,
        @NotBlank @Size(min = 6, max = 100) String password,
        @Size(max = 255) String avatarUrl,
        @Size(max = 1000) String personaSummary
) {
}
