package com.parkeasy.accounts.application;

import com.parkeasy.accounts.domain.EmailAddress;
import com.parkeasy.accounts.domain.Role;
import com.parkeasy.accounts.domain.UserId;

public interface AccountRegistrationRepository {

    UserId createUser(EmailAddress email, String displayName);

    void createCredentials(UserId userId, String passwordHash);

    void grantRole(UserId userId, Role role);
}
