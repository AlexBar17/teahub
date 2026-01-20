package com.tea.teahub.service;

import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.exception.TeaAlreadyExistException;
import com.tea.teahub.exception.TeaNotFoundException;
import com.tea.teahub.mapper.TeaMapper;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import com.tea.teahub.repository.TeaStorage;
import com.tea.teahub.service.query.TeaFilter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TeaService {
    private final TeaStorage teaStorage;
    private final TeaMapper teaMapper;

    public TeaResponse addTea(TeaRequest teaRequest) {
        teaStorage.findByName(teaRequest.name()).ifPresent(
                it -> {
                    throw new TeaAlreadyExistException(teaRequest.name());
                }
        );

        Tea tea = teaMapper.toEntity(teaRequest);

        teaStorage.save(tea);

        log.info("Чай {} с id: {} успешно добавлен", tea.getName(), tea.getId());
        return teaMapper.toResponse(tea);
    }

    public Page<TeaResponse> getTeaList(TeaFilter teaFilter, Pageable pageable) {

        Specification<Tea> specification = getSpecificationByFilter(teaFilter);

        return teaStorage.findAll(specification, pageable).
                map(teaMapper::toResponse);
    }

    public TeaResponse getTeaById(String id) {
        Tea tea = teaStorage.findById(UUID.fromString(id)).orElseThrow(
                () -> new TeaNotFoundException(id)
        );

        return teaMapper.toResponse(tea);
    }

    public TeaResponse updateTea(String id, TeaRequest teaRequest) {
        Tea tea = teaStorage.findById(UUID.fromString(id)).orElseThrow(
                () -> new TeaNotFoundException(id)
        );

        tea.setName(teaRequest.name());
        tea.setOriginCountry(teaRequest.originCountry());
        tea.setOriginRegion(teaRequest.originRegion());
        tea.setType(TeaType.valueOf(teaRequest.type()));
        tea.setNotes(teaRequest.notes());
        tea.setPrice(teaRequest.price());

        log.info("Чай {} с id: {} успешно обновлен", tea.getName(), tea.getId());
        return teaMapper.toResponse(tea);
    }

    public void deleteById(UUID id) {
        Optional<Tea> tea = teaStorage.findById(id);
        log.info("Чай с id: {} успешно удален", id);
    }

    private Specification<Tea> getSpecificationByFilter(TeaFilter teaFilter) {
        if (teaFilter == null) {
            return Specification.allOf();
        }

        Specification<Tea> specification = Specification.allOf();

        if (teaFilter.name() != null) {
            String name = teaFilter.name();
            String normalizedName = StringUtils.normalizeSpace(name);

            if (!normalizedName.isBlank()) {
                String[] words = normalizedName.split(" ");
                specification = specification.and(TeaSpecification.hasWordsInName(words));
            }
        }

        if (teaFilter.teaType() != null) {
            specification = specification.and(TeaSpecification.hasType(teaFilter.teaType()));
        }
        if (teaFilter.originCountry() != null) {
            specification = specification.and(TeaSpecification.hasOriginCountry(teaFilter.originCountry()));
        }
        if (teaFilter.originRegion() != null) {
            specification = specification.and(TeaSpecification.hasOriginRegion(teaFilter.originRegion()));
        }
        if (teaFilter.maxAmount() != null) {
            specification = specification.and(TeaSpecification.amountLessThan(teaFilter.maxAmount()));
        }
        if (teaFilter.minAmount() != null) {
            specification = specification.and(TeaSpecification.amountGreaterThan(teaFilter.minAmount()));
        }
        specification = specification.and(TeaSpecification.isActive());
        return specification;
    }

}
