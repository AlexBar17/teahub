package com.tea.teahub.controller;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new TestController())
        .setControllerAdvice(new RestExceptionHandler())
        .build();

    @Test
    void nullableConstraintName_returnsFallbackError() throws Exception {
        mockMvc.perform(get("/test/constraint-null"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("2002"));
    }

    @Test
    void unknownConstraintName_returnsFallbackError() throws Exception {
        mockMvc.perform(get("/test/constraint-unknown"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("2002"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/constraint-null")
        public void failNull() {
            var exception = new ConstraintViolationException(
                "Test constraint violation",
                new SQLException("Simulated database error"),
                null
            );

            throw new DataIntegrityViolationException(
                "Database error",
                exception
            );
        }

        @GetMapping("/test/constraint-unknown")
        public void failUnknown() {
            var exception = new ConstraintViolationException(
                "Test constraint violation",
                new SQLException("Simulated database error"),
                "uk_unknown_constraint"
            );

            throw new DataIntegrityViolationException(
                "Database error",
                exception
            );
        }
    }
}
