package com.cyber48.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserActivityDto(
        List<PostDto> posts,
        List<CommentDto> comments
) {}
