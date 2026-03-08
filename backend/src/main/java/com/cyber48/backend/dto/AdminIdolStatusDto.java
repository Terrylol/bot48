package com.cyber48.backend.dto;

import java.time.LocalDateTime;

public record AdminIdolStatusDto(
        Long idolId,
        Integer stamina,
        Integer mood,
        LocalDateTime lastActiveAt
) {
}
