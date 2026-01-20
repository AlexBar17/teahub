package com.tea.teahub.controller.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TeaRequest(
        @NotBlank String name,
        @NotBlank String originCountry,
        @NotBlank String originRegion,
        @NotBlank String type,
        @NotBlank String notes,
        @NotNull BigDecimal price
) {
}
