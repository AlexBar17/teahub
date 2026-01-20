package com.tea.teahub.mapper;

import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class TeaMapper {

    public abstract TeaResponse toResponse(Tea tea);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract Tea toEntity(TeaRequest teaRequest);

    protected String map(TeaType type) {
        return type == null ? null : type.name();
    }

    protected TeaType map(String type) {
        return type == null ? null : TeaType.valueOf(type);
    }

    protected BigDecimal map(BigDecimal amount) {
        return amount == null ? null : amount.stripTrailingZeros();
    }
}
