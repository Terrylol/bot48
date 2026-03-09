package com.cyber48.backend.repo;

import com.cyber48.backend.entity.FollowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    List<FollowEntity> findByFanId(Long fanId);
    List<FollowEntity> findByIdolId(Long idolId);
    Optional<FollowEntity> findByFanIdAndIdolId(Long fanId, Long idolId);
    boolean existsByFanIdAndIdolId(Long fanId, Long idolId);
    void deleteByFanIdAndIdolId(Long fanId, Long idolId);
    long countByIdolId(Long idolId);
}
