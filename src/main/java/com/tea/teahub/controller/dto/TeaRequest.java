package com.tea.teahub.controller.dto;

import com.tea.teahub.model.enums.TeaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TeaRequest(
        @NotBlank String name,
        @NotBlank String originCountry,
        @NotBlank String originRegion,
        @NotNull TeaType type,
        @NotBlank String notes,
        @NotNull BigDecimal price
) {
}
