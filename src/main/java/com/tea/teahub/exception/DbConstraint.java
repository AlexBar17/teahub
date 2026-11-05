package com.tea.teahub.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

@Getter
@RequiredArgsConstructor
public enum DbConstraint {

    UK_TEA_NAME(ExceptionData.TEA_ALREADY_EXIST);

    private final ExceptionData exceptionData;

    public static ExceptionData resolve(String constraintName) {
        if (constraintName == null) {
            return ExceptionData.DB_CONSTRAINT_VIOLATION;
        }
        String constraintNameUpperCase = constraintName.toUpperCase(Locale.ROOT);
        for (DbConstraint constraint : values()){
            if (constraint.name().equals(constraintNameUpperCase)){
                return constraint.exceptionData;
            }
        }
        return ExceptionData.DB_CONSTRAINT_VIOLATION;
    }
}
