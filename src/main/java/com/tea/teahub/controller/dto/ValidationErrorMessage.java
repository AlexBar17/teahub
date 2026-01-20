package com.tea.teahub.controller.dto;

import java.util.List;

public record ValidationErrorMessage(
        String code,
        String message,
        List<Violation> errors
        ) {
    public record Violation(String field, String message) {
    }
}
