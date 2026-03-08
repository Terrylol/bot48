package com.cyber48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AgentReplyRequest(
        @NotNull Long agentId,
        @NotNull Long postId,
        @NotBlank @Size(max = 500) String content
) {
}
