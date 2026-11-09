package com.tea.teahub.controller;

import com.tea.teahub.FullContext;
import com.tea.teahub.controller.dto.ValidationErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TeaRequestValidationTest extends FullContext {

    private static final String VALIDATION_ERROR_CODE = "2001";
    private static final String VALIDATION_ERROR_MESSAGE = "В запросе есть ошибки валидации";

    @Test
    void addTea_blankName_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("name", " ");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("name", "must not be blank"));
    }

    @Test
    void addTea_blankOriginCountry_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("originCountry", "");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("originCountry", "must not be blank"));
    }

    @Test
    void addTea_blankOriginRegion_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("originRegion", "");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("originRegion", "must not be blank"));
    }

    @Test
    void addTea_blankNotes_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("notes", " ");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("notes", "must not be blank"));
    }

    @Test
    void addTea_nullType_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("type", null);

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("type", "must not be null"));
    }

    @Test
    void addTea_nullPrice_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("price", null);

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("price", "must not be null"));
    }

    @Test
    void addTea_nonPositivePrice_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("price", "-2");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("price", "must be greater than or equal to 0.01"));
    }

    @Test
    void addTea_smallPrice_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("price", "0.000002");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("price", "must be greater than or equal to 0.01"));
    }

    @Test
    void addTea_zeroPrice_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("price", "0.00");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("price", "must be greater than or equal to 0.01"));
    }

    @Test
    void addTea_toBigPrice_returnsValidationError() throws Exception {
        // given
        Map<String, Object> request = validRequest();
        request.put("price", "100000000000000");

        // expect
        assertValidationError(request, new ValidationErrorMessage.Violation("price", "must be less than or equal to 10000000.00"));
    }


    private Map<String, Object> validRequest() {
        return new LinkedHashMap<>(Map.of(
            "name", "Да Хун Пао",
            "originCountry", "Китай",
            "originRegion", "Фуцзянь",
            "type", "OOLONG",
            "notes", "Жженый, минеральный",
            "price", "1200.50"
        ));
    }

    private void assertValidationError(Map<String, Object> request, ValidationErrorMessage.Violation expectedViolation)
        throws Exception {
        String contentAsString = mockMvc.perform(
                post("/api/v1/teas")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ValidationErrorMessage actual = objectMapper.readValue(contentAsString, ValidationErrorMessage.class);

        assertThat(actual.code()).isEqualTo(VALIDATION_ERROR_CODE);
        assertThat(actual.message()).isEqualTo(VALIDATION_ERROR_MESSAGE);
        assertThat(actual.errors()).containsExactly(expectedViolation);
    }
}
