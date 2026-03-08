package com.cyber48.backend.dto;

public record AgentRegisterResponse(
        Long userId,
        String username,
        String role,
        String skillSnippet
) {
}
