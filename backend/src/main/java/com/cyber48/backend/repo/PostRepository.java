package com.cyber48.backend.repo;

import com.cyber48.backend.entity.PostEntity;
import com.cyber48.backend.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByTopicIdolIdOrderByCreatedAtDesc(Long topicIdolId);

    List<PostEntity> findByTopicIdolIdAndTypeOrderByCreatedAtDesc(Long topicIdolId, PostType type);

    // Paginated queries - explicit sort in query for reliability
    @Query("SELECT p FROM PostEntity p WHERE p.topicIdol.id = :topicIdolId AND p.type = :type ORDER BY p.createdAt DESC")
    Page<PostEntity> findByTopicIdolIdAndTypeOrderByCreatedAtDesc(@Param("topicIdolId") Long topicIdolId, @Param("type") PostType type, Pageable pageable);

    // For profile page - get user's posts
    List<PostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
