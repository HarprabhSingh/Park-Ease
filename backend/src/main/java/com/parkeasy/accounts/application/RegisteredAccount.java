package com.parkeasy.accounts.application;

import java.util.Set;
import java.util.Objects;

import com.parkeasy.accounts.domain.AccountStatus;
import com.parkeasy.accounts.domain.Role;
import com.parkeasy.accounts.domain.UserId;

public record RegisteredAccount(
        UserId userId,
        String displayName,
        AccountStatus accountStatus,
        Set<Role> roles) {

    public RegisteredAccount {
        Objects.requireNonNull(userId, "User ID must not be null");
        Objects.requireNonNull(displayName, "Display name must not be null");
        Objects.requireNonNull(accountStatus, "Account status must not be null");
        roles = Set.copyOf(roles);
    }
}
