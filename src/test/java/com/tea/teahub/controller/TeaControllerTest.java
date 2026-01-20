package com.tea.teahub.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.tea.teahub.FullContext;
import com.tea.teahub.controller.dto.TeaRequest;
import com.tea.teahub.controller.dto.TeaResponse;
import com.tea.teahub.controller.dto.ValidationErrorMessage;
import com.tea.teahub.model.Tea;
import com.tea.teahub.model.enums.TeaType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class TeaControllerTest extends FullContext {

    @BeforeEach
    void cleanUp() {
        teaStorage.deleteAll();
    }

    @Test
    void addTea_validRequest_returns200AndCreate() throws Exception {
        // given
        Tea expectedTea = getGaba();
        TeaRequest ExspectedTeaRequest = getGabaRequest();

        // when
        String contentAsString = mockMvc.perform(
                        post("/api/v1/teas")
                                .content(objectMapper.writeValueAsString(ExspectedTeaRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                )

                // then
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        TeaResponse teaResponse = objectMapper.readValue(contentAsString, TeaResponse.class);

        assertNotNull(teaResponse);
        TeaResponse expected = teaMapper.toResponse(expectedTea);

        assertThat(teaResponse)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .withComparatorForType(
                        nullsFirst(BigDecimal::compareTo),
                        BigDecimal.class
                )
                .isEqualTo(expected);

        Tea teaFromStorage = teaStorage.findById(teaResponse.id()).get();

        assertThat(teaFromStorage)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .ignoringFields("createdAt")
                .ignoringFields("updatedAt")
                .withComparatorForType(
                        nullsFirst(BigDecimal::compareTo),
                        BigDecimal.class
                )
                .isEqualTo(expectedTea);
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
                            "rating": 10
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
                        new ValidationErrorMessage.Violation("rating", "must be less than or equal to 5.00"),
                        new ValidationErrorMessage.Violation("type", "must not be blank")
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
        String contentAsString = mockMvc.perform(get("/api/v1/teas?teaType=OOLONG&originCountry=China-Taiwan&originRegion=Alishan&name=Alishan Gaba"))
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

        assertEquals(expectedList, actualList);
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
        Tea errorGaba = Tea.builder()
                .name("GABA Alishani")
                .originCountry("Taiwani")
                .originRegion("Alishani")
                .type(TeaType.PUER)
                .notes("GABA processed calming effect")
                .rating(new BigDecimal("4.70"))
                .build();

        errorGaba = teaStorage.save(errorGaba);

        Tea updatedGaba = getGaba();
        TeaRequest request = getGabaRequest();

        // when
        String contentAsString = mockMvc.perform(
                        put("/api/v1/teas/" + errorGaba.getId())
                                .content(objectMapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        // then
        TeaResponse teaResponse = objectMapper.readValue(contentAsString, TeaResponse.class);

        assertNotNull(teaResponse);
        TeaResponse expected = teaMapper.toResponse(updatedGaba);

        assertThat(teaResponse)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .ignoringFields("createdAt")
                .ignoringFields("updatedAt")
                .withComparatorForType(
                        nullsFirst(BigDecimal::compareTo),
                        BigDecimal.class
                )
                .isEqualTo(expected);

        Tea teaFromStorage = teaStorage.findById(teaResponse.id()).get();

        assertThat(teaFromStorage)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .ignoringFields("createdAt")
                .ignoringFields("updatedAt")
                .withComparatorForType(
                        nullsFirst(BigDecimal::compareTo),
                        BigDecimal.class
                )
                .isEqualTo(updatedGaba);
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
    void deleteTea_existingId_returns204AndDelete() throws Exception {
        // given
        Tea gaba = getGaba();

        gaba = teaStorage.save(gaba);

        // when
        mockMvc.perform(
                        delete("/api/v1/teas/" + gaba.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                // then
                .andExpect(status().isNoContent());

        assertTrue(teaStorage.findById(gaba.getId()).isEmpty());
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
    private static final BigDecimal RATING_473 = new BigDecimal("4.73");
    private static final BigDecimal RATING_466 = new BigDecimal("4.66");

    private Tea getGaba() {
        return Tea.builder()
                .name(GABA_NAME)
                .originCountry(TAIWAN)
                .originRegion(ALISHAN)
                .type(TeaType.OOLONG)
                .notes(GABA_NOTES)
                .rating(RATING_48)
                .build();
    }

    private Tea getPuer() {
        return Tea.builder()
                .name(PUER_NAME)
                .originCountry(CHINA)
                .originRegion(YUNNAN)
                .type(TeaType.PUER)
                .notes(PUER_NOTES)
                .rating(RATING_473)
                .build();
    }

    private Tea getLongjing() {
        return Tea.builder()
                .name(LONGJING_NAME)
                .originCountry(CHINA)
                .originRegion(ZHEJIANG)
                .type(TeaType.GREEN)
                .notes(LONGJING_NOTES)
                .rating(RATING_466)
                .build();
    }

    private TeaRequest getGabaRequest() {
        return new TeaRequest(
                GABA_NAME,
                TAIWAN,
                ALISHAN,
                TeaType.OOLONG.name(),
                GABA_NOTES,
                RATING_48
        );
    }
}