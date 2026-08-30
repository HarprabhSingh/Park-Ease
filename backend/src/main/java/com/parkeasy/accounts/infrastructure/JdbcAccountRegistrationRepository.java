package com.parkeasy.accounts.infrastructure;

import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.parkeasy.accounts.application.AccountEmailAlreadyRegisteredException;
import com.parkeasy.accounts.application.AccountRegistrationRepository;
import com.parkeasy.accounts.domain.EmailAddress;
import com.parkeasy.accounts.domain.Role;
import com.parkeasy.accounts.domain.UserId;

@Repository
public class JdbcAccountRegistrationRepository implements AccountRegistrationRepository {

    private final JdbcTemplate jdbc;

    public JdbcAccountRegistrationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserId createUser(EmailAddress email, String displayName) {
        try {
            UUID generatedId = jdbc.queryForObject("""
                    INSERT INTO users (email_normalized, display_name)
                    VALUES (?, ?)
                    RETURNING user_id
                    """,
                    (resultSet, rowNumber) -> resultSet.getObject("user_id", UUID.class),
                    email.value(),
                    displayName);
            return new UserId(generatedId);
        } catch (DuplicateKeyException exception) {
            throw new AccountEmailAlreadyRegisteredException();
        }
    }

    @Override
    public void createCredentials(UserId userId, String passwordHash) {
        jdbc.update("""
                INSERT INTO user_credentials (user_id, password_hash)
                VALUES (?, ?)
                """,
                userId.value(),
                passwordHash);
    }

    @Override
    public void grantRole(UserId userId, Role role) {
        jdbc.update("""
                INSERT INTO user_roles (user_id, role_code)
                VALUES (?, ?)
                """,
                userId.value(),
                role.name());
    }
}
