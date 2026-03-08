package com.cyber48.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AgentRestRequest(
        @NotNull Long agentId
) {
}
