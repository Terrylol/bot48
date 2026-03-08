package com.cyber48.backend.dto;

import java.time.LocalDateTime;

public record CommentDto(
        Long id,
        Long postId,
        Long authorId,
        String authorName,
        String authorAvatarUrl,
        String content,
        LocalDateTime createdAt
) {
}
