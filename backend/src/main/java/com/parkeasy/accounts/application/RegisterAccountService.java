package com.parkeasy.accounts.application;

import java.text.Normalizer;

import org.springframework.stereotype.Service;

import com.parkeasy.accounts.domain.EmailAddress;

@Service
public class RegisterAccountService implements RegisterAccount {

    private static final int MINIMUM_PASSWORD_LENGTH = 15;
    private static final int MAXIMUM_PASSWORD_LENGTH = 128;
    private static final int MAXIMUM_DISPLAY_NAME_LENGTH = 100;

    private final AccountCreator accountCreator;
    private final PasswordHasher passwordHasher;

    public RegisterAccountService(
            AccountCreator accountCreator,
            PasswordHasher passwordHasher) {
        this.accountCreator = accountCreator;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public RegisteredAccount register(RegisterAccountCommand command) {
        if (command == null) {
            throw new AccountRegistrationValidationException("request", "Registration request is required");
        }

        EmailAddress email = validatedEmail(command.email());
        String displayName = validatedDisplayName(command.displayName());
        String normalizedPassword = validatedPassword(command.rawPassword());

        // Hash before entering the transactional collaborator so expensive
        // cryptographic work does not extend the database transaction.
        String passwordHash = passwordHasher.hash(normalizedPassword);

        return accountCreator.create(email, displayName, passwordHash);
    }

    private EmailAddress validatedEmail(String suppliedEmail) {
        try {
            return new EmailAddress(suppliedEmail);
        } catch (IllegalArgumentException exception) {
            throw new AccountRegistrationValidationException("email", exception.getMessage());
        }
    }

    private String validatedDisplayName(String suppliedDisplayName) {
        if (suppliedDisplayName == null) {
            throw new AccountRegistrationValidationException("displayName", "Display name is required");
        }

        String displayName = suppliedDisplayName.strip();
        if (displayName.isBlank() || displayName.length() > MAXIMUM_DISPLAY_NAME_LENGTH) {
            throw new AccountRegistrationValidationException(
                    "displayName",
                    "Display name must contain between 1 and 100 characters");
        }
        return displayName;
    }

    private String validatedPassword(String suppliedPassword) {
        if (suppliedPassword == null) {
            throw new AccountRegistrationValidationException("password", "Password is required");
        }

        String password = Normalizer.normalize(suppliedPassword, Normalizer.Form.NFC);
        int codePointLength = password.codePointCount(0, password.length());
        if (codePointLength < MINIMUM_PASSWORD_LENGTH || codePointLength > MAXIMUM_PASSWORD_LENGTH) {
            throw new AccountRegistrationValidationException(
                    "password",
                    "Password must contain between 15 and 128 characters");
        }
        return password;
    }
}
