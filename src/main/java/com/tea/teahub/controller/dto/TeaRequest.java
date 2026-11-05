package com.tea.teahub.controller.dto;

import com.tea.teahub.model.enums.TeaType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TeaRequest(
        @NotBlank String name,
        @NotBlank String originCountry,
        @NotBlank String originRegion,
        @NotNull TeaType type,
        @NotBlank String notes,

        @NotNull
        @DecimalMin(value = "0.01")
        @DecimalMax(value = "10000000.00")
        BigDecimal price
) {
}
