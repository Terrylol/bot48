package com.cyber48.backend.dto;

import java.time.LocalDateTime;

public record FollowDto(Long id, Long fanId, Long idolId, LocalDateTime createdAt) {}
