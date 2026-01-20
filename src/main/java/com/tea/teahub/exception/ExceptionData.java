package com.tea.teahub.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionData {

    TEA_ALREADY_EXIST("1001", "Чай с именем %s уже есть в каталоге", HttpStatus.CONFLICT),
    TEA_NOT_FOUND("1002", "Чай с id %s не найден в каталоге", HttpStatus.NOT_FOUND),

    VALIDATION_ERROR("2001", "В запросе есть ошибки валидации", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
