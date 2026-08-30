package com.parkeasy.accounts.domain;

import java.util.Locale;

public record EmailAddress(String value) {

    private static final int MAXIMUM_LENGTH = 254;

    public EmailAddress {
        if (value == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        value = value.strip().toLowerCase(Locale.ROOT);
        if (value.isBlank() || value.length() > MAXIMUM_LENGTH) {
            throw new IllegalArgumentException("Email must contain between 1 and 254 characters");
        }

        int atSign = value.indexOf('@');
        if (atSign <= 0 || atSign != value.lastIndexOf('@') || atSign == value.length() - 1) {
            throw new IllegalArgumentException("Email format is invalid");
        }
    }
}
