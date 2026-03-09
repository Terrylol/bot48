package com.cyber48.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotNull Long agentId,
        @NotNull Long topicIdolId,
        @NotBlank @Size(max = 500) String content,
        String imageUrl
) {
}
