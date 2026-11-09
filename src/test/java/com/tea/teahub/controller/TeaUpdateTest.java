package com.tea.teahub.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.tea.teahub.FullContext;
import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.tea.teahub.fixture.TeaTestData.*;
import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TeaUpdateTest extends FullContext {

    @BeforeEach
    void cleanUp() {
        teaStorage.deleteAll();
    }

    @Test
    void updateTea_existingId_returnsUpdatedTea() throws Exception {
        // given
        Tea teaToUpdate = teaStorage.save(Tea.builder()
            .name("GABA Alishani")
            .originCountry("Taiwani")
            .originRegion("Alishani")
            .type(TeaType.PUER)
            .notes("GABA processed calming effect")
            .price(PRICE)
            .build());
        Tea expectedTea = getGaba();
        TeaRequest request = getGabaRequest();

        // when
        String responseBody = mockMvc.perform(
                put("/api/v1/teas/{id}", teaToUpdate.getId())
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        TeaResponse response = objectMapper.readValue(responseBody, TeaResponse.class);

        assertThat(response.id()).isEqualTo(teaToUpdate.getId());
        assertTeaEqualsIgnoringGeneratedFields(response, teaMapper.toResponse(expectedTea));
        assertThat(teaStorage.findById(teaToUpdate.getId()))
            .hasValueSatisfying(
                savedTea -> {
                    assertTeaEqualsIgnoringGeneratedFields(savedTea, expectedTea);
                    assertThat(savedTea.getPrice().scale()).isEqualTo(2);
                }
            );
        assertThat(response.price().scale()).isEqualTo(2);
    }

    @Test
    void updateTea_existingIdWithExtraDecimal_returnsUpdatedTea() throws Exception {
        // given
        Tea teaToUpdate = teaStorage.save(Tea.builder()
            .name("GABA Alishani")
            .originCountry("Taiwani")
            .originRegion("Alishani")
            .type(TeaType.PUER)
            .notes("GABA processed calming effect")
            .price(PRICE)
            .build());
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
                put("/api/v1/teas/{id}", teaToUpdate.getId())
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        TeaResponse response = objectMapper.readValue(responseBody, TeaResponse.class);

        assertThat(response.id()).isEqualTo(teaToUpdate.getId());
        assertTeaEqualsIgnoringGeneratedFields(response, teaMapper.toResponse(expectedTea));
        assertThat(teaStorage.findById(teaToUpdate.getId()))
            .hasValueSatisfying(
                savedTea -> assertTeaEqualsIgnoringGeneratedFields(savedTea, expectedTea)
            );
    }

    @Test
    void updateTea_nonExistingId_returns404() throws Exception {
        //given, when
        TeaRequest request = getGabaRequest();
        UUID fakeUuid = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/teas/" + fakeUuid)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            // then
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("1002"))
            .andExpect(
                jsonPath("$.message").value("Чай с id " + fakeUuid + " не найден в каталоге")
            );
    }

    @Test
    void updateTea_existingNameOfAnotherTea_returns409() throws Exception {
        // given
        teaStorage.save(getGaba());
        Tea teaToUpdate = teaStorage.save(Tea.builder()
            .name("GABA Alishani")
            .originCountry("Taiwani")
            .originRegion("Alishani")
            .type(TeaType.PUER)
            .notes("GABA processed calming effect")
            .price(PRICE)
            .build());
        TeaRequest request = getGabaRequest();

        // when
        mockMvc.perform(put("/api/v1/teas/{id}", teaToUpdate.getId())
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
            // then
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("1001"))
            .andExpect(
                jsonPath("$.message").value("Чай с таким именем уже есть в каталоге")
            );
    }

    @Test
    void activateTea_existingId_returns204AndActivatesTea() throws Exception {
        // given
        Tea unactiveTea = Tea.builder()
            .name(GABA_NAME)
            .originCountry(TAIWAN)
            .originRegion(ALISHAN)
            .type(TeaType.OOLONG)
            .notes(GABA_NOTES)
            .price(PRICE)
            .active(false)
            .build();

        Tea savedTea = teaStorage.save(unactiveTea);

        // when
        mockMvc.perform(post("/api/v1/teas/{id}/activate", savedTea.getId()))
            // then
            .andExpect(status().isNoContent());

        assertThat(teaStorage.findById(savedTea.getId()))
            .hasValueSatisfying(tea -> assertThat(tea.getActive()).isTrue());

        var responseBody = mockMvc.perform(get("/api/v1/teas"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = objectMapper.readTree(responseBody);

        List<TeaResponse> actualList = objectMapper.readValue(
            root.get("content").toString(),
            new TypeReference<>() {
            }
        );
        assertThat(actualList.size()).isEqualTo(1);
        assertTeaEqualsIgnoringGeneratedFields(actualList.getFirst(), teaMapper.toResponse(savedTea));

    }

    @Test
    void activateTea_notExistingId_returns404() throws Exception {
        // when
        mockMvc.perform(post("/api/v1/teas/{id}/activate", UUID.randomUUID()))
            // then
            .andExpect(status().isNotFound());
    }

    @Test
    void activateTea_ActiveTea_returns204() throws Exception {
        // given
        Tea savedTea = teaStorage.save(getGaba());
        // when
        mockMvc.perform(post("/api/v1/teas/{id}/activate", savedTea.getId()))
            // then
            .andExpect(status().isNoContent());
    }

    private void assertTeaEqualsIgnoringGeneratedFields(Tea actual, Tea expected) {
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt", "version")
            .isEqualTo(expected);
    }

    private void assertTeaEqualsIgnoringGeneratedFields(
        TeaResponse actual,
        TeaResponse expected
    ) {
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt", "version")
            .withComparatorForType(nullsFirst(BigDecimal::compareTo), BigDecimal.class)
            .isEqualTo(expected);
    }
}
