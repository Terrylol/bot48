package com.cyber48.backend.repo;

import com.cyber48.backend.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    List<LikeEntity> findByPostId(Long postId);
    List<LikeEntity> findByUserId(Long userId);
    Optional<LikeEntity> findByUserIdAndPostId(Long userId, Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);
}
