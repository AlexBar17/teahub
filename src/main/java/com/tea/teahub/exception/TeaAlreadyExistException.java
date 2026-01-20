package com.tea.teahub.exception;

import static com.tea.teahub.exception.ExceptionData.TEA_ALREADY_EXIST;

public class TeaAlreadyExistException extends DomainException {

    public TeaAlreadyExistException(String name) {
        super(TEA_ALREADY_EXIST, String.format(TEA_ALREADY_EXIST.getMessage(), name));
    }
}
