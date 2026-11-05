package com.tea.teahub.exception;

import java.util.UUID;

import static com.tea.teahub.exception.ExceptionData.TEA_NOT_FOUND;

public class TeaNotFoundException extends DomainException {

    public TeaNotFoundException(UUID id) {
        super(TEA_NOT_FOUND, String.format(TEA_NOT_FOUND.getMessage(), id));
    }
}
