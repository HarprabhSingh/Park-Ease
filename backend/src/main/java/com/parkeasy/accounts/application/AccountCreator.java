package com.parkeasy.accounts.application;

import com.parkeasy.accounts.domain.EmailAddress;

public interface AccountCreator {

    RegisteredAccount create(EmailAddress email, String displayName, String passwordHash);
}
