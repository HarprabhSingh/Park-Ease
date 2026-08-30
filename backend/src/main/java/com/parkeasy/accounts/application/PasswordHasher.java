package com.parkeasy.accounts.application;

public interface PasswordHasher {

    String hash(String rawPassword);
}
