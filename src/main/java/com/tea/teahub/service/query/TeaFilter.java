package com.tea.teahub.service.query;

import com.tea.teahub.model.enums.TeaType;

public record TeaFilter(
    TeaType teaType,
    String originCountry,
    String originRegion,
    String name,
    Integer maxAmount,
    Integer minAmount
) {
}
