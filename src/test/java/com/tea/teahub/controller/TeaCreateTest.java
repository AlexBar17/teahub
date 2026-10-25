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


class TeaCreateTest extends FullContext {

    @BeforeEach
    void cleanUp() {
        teaStorage.deleteAll();
    }

    @Test
    void addTea_validRequest_returns200AndCreatesTea() throws Exception {
        // given
        Tea expectedTea = getGaba();
        TeaRequest request = getGabaRequest();

        // when
        String responseBody = mockMvc.perform(
                post("/api/v1/teas")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        TeaResponse response = objectMapper.readValue(responseBody, TeaResponse.class);
        TeaResponse expectedResponse = teaMapper.toResponse(expectedTea);

        assertThat(response)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .withComparatorForType(nullsFirst(BigDecimal::compareTo), BigDecimal.class)
            .isEqualTo(expectedResponse);

        assertThat(teaStorage.findById(response.id()))
            .hasValueSatisfying(savedTea ->
                assertTeaEqualsIgnoringGeneratedFields(savedTea, expectedTea)
            );
    }

    @Test
    void addTea_existingName_returns409() throws Exception {
        // given
        Tea savedTea = getGaba();
        teaStorage.save(savedTea);
        TeaRequest request = getGabaRequest();

        // when
        mockMvc.perform(
                post("/api/v1/teas")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )

            // then
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("1001"))
            .andExpect(
                jsonPath("$.message").value("Чай с именем GABA Alishan уже есть в каталоге")
            );

    }

    @Test
    void addTea_invalidRequest_returnsValidationErrors() throws Exception {
        // given
        String invalidJson = """
                {
                    "name": "",
                    "originCountry": " ",
                    "originRegion": "",
                    "notes": "",
                    "type": null,
                    "price": null
                }
            """;

        ValidationErrorMessage expected = new ValidationErrorMessage(
            "2001",
            "В запросе есть ошибки валидации",
            List.of(
                new ValidationErrorMessage.Violation("originRegion", "must not be blank"),
                new ValidationErrorMessage.Violation("name", "must not be blank"),
                new ValidationErrorMessage.Violation("originCountry", "must not be blank"),
                new ValidationErrorMessage.Violation("notes", "must not be blank"),
                new ValidationErrorMessage.Violation("type", "must not be null"),
                new ValidationErrorMessage.Violation("price", "must not be null")
            )
        );

        // when
        String contentAsString = mockMvc.perform(
                post("/api/v1/teas")
                    .content(invalidJson)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            // then
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ValidationErrorMessage actual =
            objectMapper.readValue(contentAsString, ValidationErrorMessage.class);

        assertThat(actual.code()).isEqualTo(expected.code());
        assertThat(actual.message()).isEqualTo(expected.message());
        assertThat(actual.errors()).containsExactlyInAnyOrderElementsOf(expected.errors());
    }

    private void assertTeaEqualsIgnoringGeneratedFields(Tea actual, Tea expected) {
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt", "version")
            .isEqualTo(expected);
    }
}
