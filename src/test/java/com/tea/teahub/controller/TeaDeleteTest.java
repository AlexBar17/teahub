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


class TeaDeleteTest extends FullContext {

    @BeforeEach
    void cleanUp() {
        teaStorage.deleteAll();
    }

    @Test
    void deleteTea_existingId_returns204AndDeactivatesTea() throws Exception {
        // given
        Tea savedTea = teaStorage.save(getGaba());

        // when
        mockMvc.perform(delete("/api/v1/teas/{id}", savedTea.getId()))
            // then
            .andExpect(status().isNoContent());

        assertThat(teaStorage.findById(savedTea.getId()))
            .hasValueSatisfying(tea -> assertThat(tea.getActive()).isFalse());
    }

    @Test
    void deleteTea_notExistingId_returns204() throws Exception {
        // when
        mockMvc.perform(delete("/api/v1/teas/{id}", UUID.randomUUID()))
            // then
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteTea_nonExistingId_returns204() throws Exception {
        // given, when
        mockMvc.perform(
                delete("/api/v1/teas/" + UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            .andExpect(status().isNoContent());
    }
}
