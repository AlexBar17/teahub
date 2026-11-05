package com.tea.teahub.controller;

import com.tea.teahub.FullContext;
import com.tea.teahub.model.Tea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.tea.teahub.fixture.TeaTestData.getGaba;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
}
