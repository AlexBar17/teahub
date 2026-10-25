package com.tea.teahub.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.tea.teahub.FullContext;
import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.controller.dto.ValidationErrorMessage;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.tea.teahub.fixture.TeaTestData.*;
import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TeaGetByIdTest extends FullContext {

    @BeforeEach
    void cleanUp() {
        teaStorage.deleteAll();
    }

    @Test
    void getTeaById_existingId_returnsTea() throws Exception {
        // given
        Tea gaba = getGaba();

        gaba = teaStorage.save(gaba);
        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas/" + gaba.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        // then
        TeaResponse teaResponse = objectMapper.readValue(contentAsString, TeaResponse.class);

        assertEquals(teaMapper.toResponse(gaba), teaResponse);
    }

    @Test
    void getTeaById_nonExistingId_returns404() throws Exception {
        //given, when
        UUID fakeUuid = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/teas/" + fakeUuid))
            // then
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("1002"))
            .andExpect(
                jsonPath("$.message").value("Чай с id " + fakeUuid + " не найден в каталоге")
            );
    }
}
