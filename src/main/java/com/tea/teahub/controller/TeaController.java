package com.tea.teahub.controller;

import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.enums.TeaType;
import com.tea.teahub.service.TeaService;
import com.tea.teahub.service.query.TeaFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/teas")
@RequiredArgsConstructor
public class TeaController {
    private final TeaService teaService;

    @PostMapping
    TeaResponse addTea(@RequestBody @Valid TeaRequest teaRequest) {
        log.info("Запрос на добавление чая с name: {}", teaRequest.name());
        return teaService.addTea(teaRequest);
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void activateTea(@PathVariable UUID id) {
        log.info("Запрос на активацию чая с id: {}", id);
        teaService.activateTeaById(id);
    }

    @GetMapping
    PagedModel<TeaResponse> getTeaList(
        @RequestParam(required = false) TeaType teaType,
        @RequestParam(required = false) String originCountry,
        @RequestParam(required = false) String originRegion,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer maxPrice,
        @RequestParam(required = false) Integer minPrice,
        @ParameterObject
        @PageableDefault(sort = "name") Pageable pageable) {

        TeaFilter filter = new TeaFilter(
            teaType,
            originCountry,
            originRegion,
            name,
            maxPrice,
            minPrice
        );
        log.info("Запрос на получение чая по фильтру: {}", filter);
        return new PagedModel<>(
            teaService.getTeaList(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    TeaResponse getTeaById(@PathVariable UUID id) {
        log.info("Запрос на получение чая с id: {}", id);
        return teaService.getTeaById(id);
    }

    @PutMapping("/{id}")
    TeaResponse updateTea(
        @PathVariable UUID id,
        @RequestBody @Valid TeaRequest teaRequest) {
        log.info("Запрос на обновление чая с id: {}", id);
        return teaService.updateTea(id, teaRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTea(@PathVariable UUID id) {
        log.info("Запрос на удаление чая с id: {}", id);
        teaService.deleteById(id);
    }

}
