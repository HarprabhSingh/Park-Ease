package com.parkeasy.accounts.web;

import java.util.Set;
import java.util.UUID;

public record RegistrationResponse(
        UUID userId,
        String displayName,
        String accountStatus,
        Set<String> roles) {
}
