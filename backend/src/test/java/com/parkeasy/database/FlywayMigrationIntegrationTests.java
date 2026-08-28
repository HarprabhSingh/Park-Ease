package com.parkeasy.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
class FlywayMigrationIntegrationTests {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.4-bookworm")
            .withDatabaseName("parkeasy_test")
            .withUsername("parkeasy_test")
            .withPassword("parkeasy_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void flywayAppliesBookingConstraintPrerequisite(@Autowired DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Integer successfulMigrationCount = jdbc.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE success",
                Integer.class);
        Boolean btreeGistInstalled = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'btree_gist')",
                Boolean.class);

        assertThat(successfulMigrationCount).isEqualTo(2);
        assertThat(btreeGistInstalled).isTrue();
    }

    @Test
    void databaseGeneratesUuidV7AndStoresACompleteAccount(@Autowired DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        UUID userId = jdbc.queryForObject("""
                INSERT INTO users (email_normalized, display_name)
                VALUES (?, ?)
                RETURNING user_id
                """,
                (resultSet, rowNumber) -> resultSet.getObject("user_id", UUID.class),
                "driver@example.com",
                "Test Driver");

        jdbc.update("""
                INSERT INTO user_credentials (user_id, password_hash)
                VALUES (?, ?)
                """, userId, "$test$not-a-real-password-hash");
        jdbc.update("""
                INSERT INTO user_roles (user_id, role_code)
                VALUES (?, 'DRIVER')
                """, userId);

        Integer uuidVersion = jdbc.queryForObject(
                "SELECT uuid_extract_version(?)",
                Integer.class,
                userId);
        String accountStatus = jdbc.queryForObject(
                "SELECT account_status FROM users WHERE user_id = ?",
                String.class,
                userId);
        Integer credentialCount = jdbc.queryForObject(
                "SELECT count(*) FROM user_credentials WHERE user_id = ?",
                Integer.class,
                userId);
        Integer roleCount = jdbc.queryForObject(
                "SELECT count(*) FROM user_roles WHERE user_id = ? AND role_code = 'DRIVER'",
                Integer.class,
                userId);

        assertThat(uuidVersion).isEqualTo(7);
        assertThat(accountStatus).isEqualTo("ACTIVE");
        assertThat(credentialCount).isEqualTo(1);
        assertThat(roleCount).isEqualTo(1);
    }

    @Test
    void databaseRejectsInvalidAccountValues(@Autowired DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO users (email_normalized, display_name)
                VALUES (' Not-Normalized@Example.com ', 'Test User')
                """))
                .isInstanceOf(DataAccessException.class);

        UUID userId = jdbc.queryForObject("""
                INSERT INTO users (email_normalized, display_name)
                VALUES ('role-test@example.com', 'Role Test User')
                RETURNING user_id
                """,
                (resultSet, rowNumber) -> resultSet.getObject("user_id", UUID.class));

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO user_roles (user_id, role_code)
                VALUES (?, 'SUPERUSER')
                """, userId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void failedRegistrationTransactionLeavesNoPartialAccount(@Autowired DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        TransactionTemplate transaction = new TransactionTemplate(
                new DataSourceTransactionManager(dataSource));
        String email = "rollback-test@example.com";

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            UUID userId = jdbc.queryForObject("""
                    INSERT INTO users (email_normalized, display_name)
                    VALUES (?, 'Rollback Test User')
                    RETURNING user_id
                    """,
                    (resultSet, rowNumber) -> resultSet.getObject("user_id", UUID.class),
                    email);

            jdbc.update("""
                    INSERT INTO user_credentials (user_id, password_hash)
                    VALUES (?, '$test$not-a-real-password-hash')
                    """, userId);
            jdbc.update("""
                    INSERT INTO user_roles (user_id, role_code)
                    VALUES (?, 'INVALID_ROLE')
                    """, userId);
        })).isInstanceOf(DataAccessException.class);

        Integer remainingUserCount = jdbc.queryForObject(
                "SELECT count(*) FROM users WHERE email_normalized = ?",
                Integer.class,
                email);

        assertThat(remainingUserCount).isZero();
    }
}
