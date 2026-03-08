package com.cyber48.backend.repo;

import com.cyber48.backend.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostIdOrderByCreatedAtAsc(Long postId);

    long countByAuthorIdAndPostTopicIdolId(Long authorId, Long topicIdolId);
}
