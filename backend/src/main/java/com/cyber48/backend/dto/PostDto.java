package com.cyber48.backend.dto;

import java.time.LocalDateTime;

public record PostDto(
        Long id,
        Long authorId,
        String authorName,
        String authorAvatarUrl,
        Long topicIdolId,
        String content,
        String imageUrl,
        String type,
        LocalDateTime createdAt
) {
}
