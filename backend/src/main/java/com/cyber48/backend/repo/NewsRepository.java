package com.cyber48.backend.repo;

import com.cyber48.backend.entity.NewsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface NewsRepository extends JpaRepository<NewsEntity, Long> {
    List<NewsEntity> findByPublishedDateOrderByCreatedAtDesc(LocalDate date);
    List<NewsEntity> findAllByOrderByCreatedAtDesc();
}
