package com.tea.teahub.controller.dto;

import com.tea.teahub.model.enums.TeaType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(
    requiredProperties = {
        "id",
        "name",
        "originCountry",
        "originRegion",
        "type",
        "notes",
        "price",
        "createdAt",
        "updatedAt"
    }
)
public record TeaResponse(
        UUID id,
        String name,
        String originCountry,
        String originRegion,
        TeaType type,
        String notes,
        BigDecimal price,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
