package com.tea.teahub.controller;

import com.tea.teahub.FullContext;
import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;

import static com.tea.teahub.fixture.TeaTestData.*;
import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            .hasValueSatisfying(
                savedTea -> {
                    assertTeaEqualsIgnoringGeneratedFields(savedTea, expectedTea);
                    assertThat(savedTea.getPrice().scale()).isEqualTo(2);
                    assertThat(response.createdAt()).isNotNull();
                    assertThat(response.updatedAt()).isNotNull();
                }
            );
        assertThat(response.price().scale()).isEqualTo(2);
    }

    @Test
    void addTea_validRequestWithExtraDecimal_returns200AndCreatesTea_and_normalizePriceToMore() throws Exception {
        // given
        Tea expectedTea = Tea.builder()
            .name(GABA_NAME)
            .originCountry(TAIWAN)
            .originRegion(ALISHAN)
            .type(TeaType.OOLONG)
            .notes(GABA_NOTES)
            .price(new BigDecimal("100.13"))
            .build();
        TeaRequest request = new TeaRequest(
            GABA_NAME,
            TAIWAN,
            ALISHAN,
            TeaType.OOLONG,
            GABA_NOTES,
            new BigDecimal("100.125")
        );

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
    void addTea_validRequestWithExtraDecimal_returns200AndCreatesTea_and_normalizePriceToLess() throws Exception {
        // given
        Tea expectedTea = Tea.builder()
            .name(GABA_NAME)
            .originCountry(TAIWAN)
            .originRegion(ALISHAN)
            .type(TeaType.OOLONG)
            .notes(GABA_NOTES)
            .price(new BigDecimal("100.13"))
            .build();
        TeaRequest request = new TeaRequest(
            GABA_NAME,
            TAIWAN,
            ALISHAN,
            TeaType.OOLONG,
            GABA_NOTES,
            new BigDecimal("100.131")
        );

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
    void addTea_nameOfDeactivatedTea_returns409() throws Exception {
        // given
        Tea savedTea = teaStorage.save(getGaba());
        mockMvc.perform(delete("/api/v1/teas/{id}", savedTea.getId()))
            .andExpect(status().isNoContent());
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
                jsonPath("$.message").value("Чай с таким именем уже есть в каталоге")
            );
    }

    @Test
    void addTea_withExistingName_returns409() throws Exception {
        // given
        teaStorage.save(getGaba());
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
                jsonPath("$.message").value("Чай с таким именем уже есть в каталоге")
            );
    }

    private void assertTeaEqualsIgnoringGeneratedFields(Tea actual, Tea expected) {
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt", "version")
            .isEqualTo(expected);
    }
}
