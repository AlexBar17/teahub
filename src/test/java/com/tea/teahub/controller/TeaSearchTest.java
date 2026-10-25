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


class TeaSearchTest extends FullContext {

    @BeforeEach
    void cleanUp() {
        teaStorage.deleteAll();
    }

    @Test
    void getTeaList_noFilter_returnsSortedByName() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();

        teaStorage.saveAll(List.of(puer, longjing, gaba));

        List<TeaResponse> expectedList = List.of(
            teaMapper.toResponse(gaba),
            teaMapper.toResponse(longjing),
            teaMapper.toResponse(puer)
        );
        String contentAsString = mockMvc.perform(get("/api/v1/teas"))

            // then
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        JsonNode root = objectMapper.readTree(contentAsString);

        List<TeaResponse> actualList = objectMapper.readValue(
            root.get("content").toString(),
            new TypeReference<>() {
            }
        );


        assertThat(actualList)
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .build()
            )
            .containsExactlyInAnyOrderElementsOf(expectedList);

        assertThat(actualList)
            .extracting(TeaResponse::name)
            .isSortedAccordingTo(String::compareTo);
    }

    @Test
    void getTeaList_teaTypeFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("teaType", "OOLONG"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, gaba);
    }

    @Test
    void getTeaList_originCountryFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("originCountry", TAIWAN))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, gaba);
    }

    @Test
    void getTeaList_originRegionFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("originRegion", ALISHAN))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, gaba);
    }

    @Test
    void getTeaList_nameFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("name", "Alishan Gaba"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, gaba);
    }

    @Test
    void getTeaList_maxPriceFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();
        longjing.setPrice(new BigDecimal("150"));
        puer.setPrice(new BigDecimal("200"));

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("maxPrice", "101"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, gaba);
    }

    @Test
    void getTeaList_minPriceFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();
        longjing.setPrice(new BigDecimal("50"));
        puer.setPrice(new BigDecimal("75"));

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("minPrice", "99"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, gaba);
    }

    private void assertTeaListContainsExactly(String responseBody, Tea... expectedTeas) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        List<TeaResponse> actualList = objectMapper.readValue(
            root.get("content").toString(),
            new TypeReference<>() {
            }
        );
        List<TeaResponse> expectedList = List.of(expectedTeas).stream()
            .map(teaMapper::toResponse)
            .toList();

        assertThat(actualList)
            .usingRecursiveFieldByFieldElementComparator(
                RecursiveComparisonConfiguration.builder()
                    .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                    .build()
            )
            .containsExactlyInAnyOrderElementsOf(expectedList);
    }
}
