package com.cyber48.backend.service;

import com.cyber48.backend.dto.CommentDto;
import com.cyber48.backend.dto.IdolSummaryDto;
import com.cyber48.backend.dto.PostDto;
import com.cyber48.backend.entity.CommentEntity;
import com.cyber48.backend.entity.IdolStatusEntity;
import com.cyber48.backend.entity.PostEntity;
import com.cyber48.backend.entity.UserEntity;

public final class DtoMapper {
    private DtoMapper() {
    }

    public static IdolSummaryDto toIdolSummary(UserEntity user, IdolStatusEntity status) {
        return new IdolSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getAvatarUrl(),
                user.getPersonaSummary(),
                status.getStamina(),
                status.getMood()
        );
    }

    public static PostDto toPostDto(PostEntity post) {
        return new PostDto(
                post.getId(),
                post.getAuthor().getId(),
                post.getAuthor().getUsername(),
                post.getAuthor().getAvatarUrl(),
                post.getTopicIdol().getId(),
                post.getContent(),
                post.getImageUrl(),
                post.getType().name(),
                post.getCreatedAt()
        );
    }

    public static CommentDto toCommentDto(CommentEntity comment) {
        return new CommentDto(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getAvatarUrl(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
