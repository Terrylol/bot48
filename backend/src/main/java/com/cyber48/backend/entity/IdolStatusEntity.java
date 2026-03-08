package com.cyber48.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "idol_status")
public class IdolStatusEntity {
    @Id
    @Column(name = "idol_id")
    private Long idolId;

    @Column(nullable = false)
    private Integer stamina;

    @Column(nullable = false)
    private Integer mood;

    @Column(nullable = false)
    private LocalDateTime lastActiveAt;

    public Long getIdolId() {
        return idolId;
    }

    public void setIdolId(Long idolId) {
        this.idolId = idolId;
    }

    public Integer getStamina() {
        return stamina;
    }

    public void setStamina(Integer stamina) {
        this.stamina = stamina;
    }

    public Integer getMood() {
        return mood;
    }

    public void setMood(Integer mood) {
        this.mood = mood;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
