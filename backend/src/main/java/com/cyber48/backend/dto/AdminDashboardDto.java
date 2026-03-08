package com.cyber48.backend.dto;

import java.util.List;

public record AdminDashboardDto(
        List<AdminUserDto> users,
        List<AdminIdolStatusDto> idolStatuses,
        List<PostDto> posts,
        List<CommentDto> comments
) {
}
