package com.parkeasy.accounts.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(max = 254) String email,
        @NotNull String password,
        @NotBlank @Size(max = 100) String displayName) {

    @Override
    public String toString() {
        return "RegistrationRequest[REDACTED]";
    }
}
