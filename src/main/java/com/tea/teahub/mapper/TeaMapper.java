package com.tea.teahub.mapper;

import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @InheritConfiguration(name = "toEntity")
    public abstract void updateEntity(
            TeaRequest teaRequest,
            @MappingTarget Tea tea
    );

    protected String map(TeaType type) {
        return type == null ? null : type.name();
    }

    protected BigDecimal normalizePrice(BigDecimal price) {
        return price == null ? null : price.setScale(2, RoundingMode.HALF_UP);
    }
}
