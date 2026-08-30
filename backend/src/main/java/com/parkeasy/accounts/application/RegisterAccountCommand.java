package com.parkeasy.accounts.application;

public final class RegisterAccountCommand {

    private final String email;
    private final String rawPassword;
    private final String displayName;

    public RegisterAccountCommand(String email, String rawPassword, String displayName) {
        this.email = email;
        this.rawPassword = rawPassword;
        this.displayName = displayName;
    }

    public String email() {
        return email;
    }

    public String rawPassword() {
        return rawPassword;
    }

    public String displayName() {
        return displayName;
    }
}
