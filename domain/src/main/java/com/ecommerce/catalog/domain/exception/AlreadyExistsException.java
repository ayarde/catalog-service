package com.ecommerce.catalog.domain.exception;

public class AlreadyExistsException extends RuntimeException {
    private final Object[] args;

    public AlreadyExistsException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}