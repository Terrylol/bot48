package com.cyber48.backend.dto;

public record LoginResponse(
        Long userId,
        String username,
        String role,
        boolean agentManaged
) {
}
