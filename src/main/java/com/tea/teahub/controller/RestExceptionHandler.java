package com.tea.teahub.controller;

import com.tea.teahub.controller.dto.ErrorMessage;
import com.tea.teahub.controller.dto.ValidationErrorMessage;
import com.tea.teahub.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import static com.tea.teahub.exception.ExceptionData.VALIDATION_ERROR;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorMessage> handeDomain(DomainException domainException) {
        return new ResponseEntity<>(
                new ErrorMessage(domainException.getCode(), domainException.getMessage()),
                domainException.getHttpStatus()
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
