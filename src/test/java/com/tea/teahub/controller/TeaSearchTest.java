package com.tea.teahub.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.tea.teahub.FullContext;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.model.Tea;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static com.tea.teahub.fixture.TeaTestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.size").value(10))
            .andExpect(jsonPath("$.page.totalElements").value(3))
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
    void getTeaList_maxPriceFilter_includesBoundaryPrice() throws Exception {
        // given
        List<Tea> teas = saveTeasAtPriceBoundaries();

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("maxPrice", "1000"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, teas.get(0), teas.get(1));
    }

    @Test
    void getTeaList_minPriceFilter_includesBoundaryPrice() throws Exception {
        // given
        List<Tea> teas = saveTeasAtPriceBoundaries();

        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("minPrice", "1000"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
        assertTeaListContainsExactly(contentAsString, teas.get(1), teas.get(2));
    }

    @Test
    void getTeaList_equalMinAndMaxPrice_returnsBoundaryPrice() throws Exception {
        List<Tea> teas = saveTeasAtPriceBoundaries();

        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("minPrice", "1000")
                .param("maxPrice", "1000"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertTeaListContainsExactly(contentAsString, teas.get(1));
    }

    @Test
    void getTeaList_priceRange_returnsPricesWithinBounds() throws Exception {
        List<Tea> teas = saveTeasAtPriceBoundaries();

        String contentAsString = mockMvc.perform(get("/api/v1/teas")
                .param("minPrice", "999")
                .param("maxPrice", "1001"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertTeaListContainsExactly(contentAsString, teas.toArray(Tea[]::new));
    }

    private List<Tea> saveTeasAtPriceBoundaries() {
        Tea belowBoundary = getGaba();
        Tea atBoundary = getLongjing();
        Tea aboveBoundary = getPuer();
        belowBoundary.setPrice(new BigDecimal("999.99"));
        atBoundary.setPrice(new BigDecimal("1000.00"));
        aboveBoundary.setPrice(new BigDecimal("1000.01"));
        return teaStorage.saveAll(List.of(belowBoundary, atBoundary, aboveBoundary));
    }

    private void assertTeaListContainsExactly(String responseBody, Tea... expectedTeas) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        List<TeaResponse> actualList = objectMapper.readValue(
            root.get("content").toString(),
            new TypeReference<>() {
            }
        );
        List<TeaResponse> expectedList = Stream.of(expectedTeas)
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
