package com.parkeasy.accounts.application;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkeasy.accounts.domain.AccountStatus;
import com.parkeasy.accounts.domain.EmailAddress;
import com.parkeasy.accounts.domain.Role;
import com.parkeasy.accounts.domain.UserId;

@Service
public class TransactionalAccountCreator implements AccountCreator {

    private final AccountRegistrationRepository repository;

    public TransactionalAccountCreator(AccountRegistrationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public RegisteredAccount create(EmailAddress email, String displayName, String passwordHash) {
        UserId userId = repository.createUser(email, displayName);
        repository.createCredentials(userId, passwordHash);
        repository.grantRole(userId, Role.DRIVER);

        return new RegisteredAccount(
                userId,
                displayName,
                AccountStatus.ACTIVE,
                Set.of(Role.DRIVER));
    }
}
