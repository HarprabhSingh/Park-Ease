package com.parkeasy.accounts.application;

public final class AccountRegistrationValidationException extends RuntimeException {

    private final String field;

    public AccountRegistrationValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
