package com.parkeasy.accounts.web;

import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkeasy.accounts.application.RegisterAccount;
import com.parkeasy.accounts.application.RegisterAccountCommand;
import com.parkeasy.accounts.application.RegisteredAccount;

@RestController
@RequestMapping("/api/v1/auth/registrations")
public class RegistrationController {

    private final RegisterAccount registerAccount;

    public RegistrationController(RegisterAccount registerAccount) {
        this.registerAccount = registerAccount;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request) {
        RegisteredAccount account = registerAccount.register(new RegisterAccountCommand(
                request.email(),
                request.password(),
                request.displayName()));

        RegistrationResponse response = new RegistrationResponse(
                account.userId().value(),
                account.displayName(),
                account.accountStatus().name(),
                account.roles().stream()
                        .map(Enum::name)
                        .collect(Collectors.toUnmodifiableSet()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
