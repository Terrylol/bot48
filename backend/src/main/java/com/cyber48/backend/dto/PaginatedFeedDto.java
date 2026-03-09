package com.cyber48.backend.dto;

import java.util.List;

public record PaginatedFeedDto(
        Long topicIdolId,
        List<PostDto> idolPosts,
        List<PostDto> fanPosts,
        int idolPage,
        int idolPageSize,
        long idolTotal,
        int fanPage,
        int fanPageSize,
        long fanTotal,
        int idolTotalPages,
        int fanTotalPages
) {
}
