package com.cyber48.backend.dto;

import java.util.List;

public record FeedDto(
        Long topicIdolId,
        List<PostDto> idolPosts,
        List<PostDto> fanPosts
) {
}
