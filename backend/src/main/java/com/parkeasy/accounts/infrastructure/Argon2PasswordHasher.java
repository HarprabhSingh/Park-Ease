package com.parkeasy.accounts.infrastructure;

import java.util.Map;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.parkeasy.accounts.application.PasswordHasher;

@Component
public class Argon2PasswordHasher implements PasswordHasher {

    private static final String ENCODER_ID = "argon2@SpringSecurity_v5_8";

    private final PasswordEncoder encoder;

    public Argon2PasswordHasher() {
        PasswordEncoder argon2 = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.encoder = new DelegatingPasswordEncoder(ENCODER_ID, Map.of(ENCODER_ID, argon2));
    }

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
