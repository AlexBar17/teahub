package com.tea.teahub.controller;

import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.enums.TeaType;
import com.tea.teahub.service.TeaService;
import com.tea.teahub.service.query.TeaFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    Page<TeaResponse> getTeaList(
            @RequestParam(required = false) TeaType teaType,
            @RequestParam(required = false) String originCountry,
            @RequestParam(required = false) String originRegion,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = "name") Pageable pageable) {

        TeaFilter filter = new TeaFilter(teaType, originCountry, originRegion, name);
        log.info("Запрос на получение чая по фильтру: {}", filter);
        return teaService.getTeaList(filter, pageable);
    }

    @GetMapping("/{id}")
    TeaResponse getTeaById(@PathVariable String id) {
        log.info("Запрос на получение чая с id: {}", id);
        return teaService.getTeaById(id);
    }

    @PutMapping("/{id}")
    TeaResponse updateTea(
            @PathVariable String id,
            @RequestBody @Valid TeaRequest teaRequest) {
        log.info("Запрос на обновление чая с id: {}", id);
        return teaService.updateTea(id, teaRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTea(@PathVariable String id) {
        log.info("Запрос на удаление чая с id: {}", id);
        teaService.deleteById(id);
    }

}
