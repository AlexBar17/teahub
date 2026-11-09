package com.tea.teahub.controller;

import com.tea.teahub.controller.dto.ErrorMessage;
import com.tea.teahub.controller.dto.ValidationErrorMessage;
import com.tea.teahub.exception.DbConstraint;
import com.tea.teahub.exception.DomainException;
import com.tea.teahub.exception.ExceptionData;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static com.tea.teahub.exception.ExceptionData.VALIDATION_ERROR;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorMessage> handeDomain(DomainException domainException) {
        return new ResponseEntity<>(
            new ErrorMessage(domainException.getCode(), domainException.getMessage()),
            domainException.getHttpStatus()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolation(ConstraintViolationException constraintViolationException) {
        String constraintName = constraintViolationException.getConstraintName();
        ExceptionData exceptionData = DbConstraint.resolve(constraintName);

        if (exceptionData == ExceptionData.DB_CONSTRAINT_VIOLATION) {
            log.error("Unknown database constraint violation: {}", constraintName, constraintViolationException);
        } else {
            log.warn("Database constraint violation: {}", constraintName);
        }

        return new ResponseEntity<>(
            new ErrorMessage(exceptionData.getCode(), exceptionData.getMessage()),
            exceptionData.getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorMessage> handleDtoValidation(MethodArgumentNotValidException methodArgumentNotValidException) {
        List<ValidationErrorMessage.Violation> errors =
            methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                .map(it -> new ValidationErrorMessage.Violation(
                    it.getField(),
                    it.getDefaultMessage()
                ))
                .toList();

        ValidationErrorMessage body = new ValidationErrorMessage(
            VALIDATION_ERROR.getCode(),
            VALIDATION_ERROR.getMessage(),
            errors
        );
        return ResponseEntity
            .status(VALIDATION_ERROR.getHttpStatus())
            .body(body);
    }
}
