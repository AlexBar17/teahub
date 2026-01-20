package com.tea.teahub.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record   TeaResponse(
        UUID id,
        String name,
        String originCountry,
        String originRegion,
        String type,
        String notes,
        BigDecimal rating,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
