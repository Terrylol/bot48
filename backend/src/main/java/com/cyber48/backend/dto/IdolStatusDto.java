package com.cyber48.backend.dto;

public record IdolStatusDto(
        Long idolId,
        Integer stamina,
        Integer mood
) {
}
