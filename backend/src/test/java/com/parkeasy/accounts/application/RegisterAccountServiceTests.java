package com.parkeasy.accounts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.parkeasy.accounts.domain.AccountStatus;
import com.parkeasy.accounts.domain.EmailAddress;
import com.parkeasy.accounts.domain.Role;
import com.parkeasy.accounts.domain.UserId;

class RegisterAccountServiceTests {

    @Test
    void normalizesInputAndPassesOnlyTheHashToAccountCreation() {
        AtomicReference<String> passwordSeenByHasher = new AtomicReference<>();
        AtomicReference<CreationArguments> creation = new AtomicReference<>();

        PasswordHasher hasher = rawPassword -> {
            passwordSeenByHasher.set(rawPassword);
            return "encoded-password";
        };
        AccountCreator creator = (email, displayName, passwordHash) -> {
            creation.set(new CreationArguments(email, displayName, passwordHash));
            return new RegisteredAccount(
                    new UserId(UUID.fromString("0195f220-8f7c-7d1e-8000-000000000001")),
                    displayName,
                    AccountStatus.ACTIVE,
                    Set.of(Role.DRIVER));
        };

        RegisterAccount service = new RegisterAccountService(creator, hasher);
        RegisteredAccount result = service.register(new RegisterAccountCommand(
                " Driver@Example.com ",
                "a sufficiently long passphrase",
                "  Example Driver  "));

        assertThat(passwordSeenByHasher.get()).isEqualTo("a sufficiently long passphrase");
        assertThat(creation.get().email().value()).isEqualTo("driver@example.com");
        assertThat(creation.get().displayName()).isEqualTo("Example Driver");
        assertThat(creation.get().passwordHash()).isEqualTo("encoded-password");
        assertThat(result.roles()).containsExactly(Role.DRIVER);
    }

    @Test
    void rejectsShortPasswordBeforeHashingOrPersistence() {
        AtomicReference<Boolean> hasherCalled = new AtomicReference<>(false);
        AtomicReference<Boolean> creatorCalled = new AtomicReference<>(false);

        PasswordHasher hasher = rawPassword -> {
            hasherCalled.set(true);
            return "should-not-be-created";
        };
        AccountCreator creator = (email, displayName, passwordHash) -> {
            creatorCalled.set(true);
            throw new AssertionError("Account creator must not be called");
        };

        RegisterAccount service = new RegisterAccountService(creator, hasher);

        assertThatThrownBy(() -> service.register(new RegisterAccountCommand(
                "driver@example.com",
                "too short",
                "Example Driver")))
                .isInstanceOf(AccountRegistrationValidationException.class)
                .hasMessage("Password must contain between 15 and 128 characters");
        assertThat(hasherCalled.get()).isFalse();
        assertThat(creatorCalled.get()).isFalse();
    }

    private record CreationArguments(
            EmailAddress email,
            String displayName,
            String passwordHash) {
    }
}
