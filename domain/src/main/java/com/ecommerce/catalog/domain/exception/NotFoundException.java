package com.ecommerce.catalog.domain.exception;

public class NotFoundException extends RuntimeException {
    private final Object[] args;

    public NotFoundException(String message, Object... args) {
        super(message);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
