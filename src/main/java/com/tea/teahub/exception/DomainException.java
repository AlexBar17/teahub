package com.tea.teahub.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DomainException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    public DomainException(ExceptionData exceptionData, String message) {
        super(message);
        this.code = exceptionData.getCode();
        this.httpStatus = exceptionData.getHttpStatus();
    }
}
