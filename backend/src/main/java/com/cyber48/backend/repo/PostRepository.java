package com.cyber48.backend.repo;

import com.cyber48.backend.entity.PostEntity;
import com.cyber48.backend.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByTopicIdolIdOrderByCreatedAtDesc(Long topicIdolId);

    List<PostEntity> findByTopicIdolIdAndTypeOrderByCreatedAtDesc(Long topicIdolId, PostType type);

    // Paginated queries
    Page<PostEntity> findByTopicIdolIdAndType(Long topicIdolId, PostType type, Pageable pageable);

    // For profile page - get user's posts
    List<PostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
