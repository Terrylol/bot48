package com.cyber48.backend.dto;

import java.time.LocalDateTime;

public record LikeDto(Long id, Long userId, Long postId, LocalDateTime createdAt) {}
