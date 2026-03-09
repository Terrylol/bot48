package com.cyber48.backend.dto;

import java.time.LocalDateTime;

public record NotificationDto(
    Long id,
    Long userId,
    String type,
    Long sourceUserId,
    String sourceUsername,
    Long targetPostId,
    Long targetCommentId,
    boolean isRead,
    LocalDateTime createdAt
) {}
