package com.parkeasy.accounts.application;

public final class AccountEmailAlreadyRegisteredException extends RuntimeException {

    public AccountEmailAlreadyRegisteredException() {
        super("An account with this email already exists");
    }
}
