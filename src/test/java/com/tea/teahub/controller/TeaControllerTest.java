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

import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TeaControllerTest extends FullContext {

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
                        "type": "",
                        "notes": "",
                        "price": ""
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
                new ValidationErrorMessage.Violation("type", "must not be blank"),
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
    void getTeaList_fullFilter_returnsMatchingTea() throws Exception {
        // given
        Tea gaba = getGaba();
        Tea longjing = getLongjing();
        Tea puer = getPuer();

        teaStorage.saveAll(List.of(gaba, longjing, puer));

        List<TeaResponse> expectedList = List.of(
            teaMapper.toResponse(gaba)
        );
        // when
        String contentAsString = mockMvc.perform(get("/api/v1/teas?teaType=OOLONG&originCountry=China-Taiwan" +
                "&originRegion=Alishan&name=Alishan Gaba&maxPrice=10&minPrice=1&minRating=4.70"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // then
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

    private void assertTeaEqualsIgnoringGeneratedFields(Tea actual, Tea expected) {
        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id", "createdAt", "updatedAt", "version")
            .withComparatorForType(nullsFirst(BigDecimal::compareTo), BigDecimal.class)
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

    private static final String GABA_NAME = "GABA Alishan";
    private static final String PUER_NAME = "Shu Pu-erh Menghai";
    private static final String LONGJING_NAME = "Longjing";

    private static final String TAIWAN = "China-Taiwan";
    private static final String CHINA = "China";

    private static final String ALISHAN = "Alishan";
    private static final String YUNNAN = "Yunnan";
    private static final String ZHEJIANG = "Zhejiang";

    private static final String GABA_NOTES =
        "GABA processed, creamy, calming effect";
    private static final String PUER_NOTES =
        "Earthy, woody, smooth body";
    private static final String LONGJING_NOTES =
        "Chestnut aroma, fresh and sweet aftertaste";

    private static final BigDecimal RATING_48 = new BigDecimal("4.8");
    private static final BigDecimal PRICE = new BigDecimal("100");


    private Tea getGaba() {
        return Tea.builder()
            .name(GABA_NAME)
            .originCountry(TAIWAN)
            .originRegion(ALISHAN)
            .type(TeaType.OOLONG)
            .notes(GABA_NOTES)
            .price(PRICE)
            .build();
    }

    private Tea getPuer() {
        return Tea.builder()
            .name(PUER_NAME)
            .originCountry(CHINA)
            .originRegion(YUNNAN)
            .type(TeaType.PUER)
            .notes(PUER_NOTES)
            .price(PRICE)
            .build();
    }

    private Tea getLongjing() {
        return Tea.builder()
            .name(LONGJING_NAME)
            .originCountry(CHINA)
            .originRegion(ZHEJIANG)
            .type(TeaType.GREEN)
            .notes(LONGJING_NOTES)
            .price(PRICE)
            .build();
    }

    private TeaRequest getGabaRequest() {
        return new TeaRequest(
            GABA_NAME,
            TAIWAN,
            ALISHAN,
            TeaType.OOLONG.name(),
            GABA_NOTES,
            PRICE
        );
    }
}
