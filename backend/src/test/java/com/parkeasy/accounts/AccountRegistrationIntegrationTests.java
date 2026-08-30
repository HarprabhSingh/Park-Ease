package com.parkeasy.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.parkeasy.accounts.application.AccountCreator;
import com.parkeasy.accounts.domain.EmailAddress;
import com.parkeasy.web.RequestCorrelationFilter;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountRegistrationIntegrationTests {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.4-bookworm")
            .withDatabaseName("parkeasy_accounts_test")
            .withUsername("parkeasy_accounts_test")
            .withPassword("parkeasy_accounts_test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int serverPort;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccountCreator accountCreator;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void registersAnAccountThroughHttpAndPersistsProtectedCredentials() throws Exception {
        String rawPassword = "a long registration passphrase";

        HttpResponse<String> response = postRegistration("""
                {
                  "email": " Driver.Http@Example.com ",
                  "password": "%s",
                  "displayName": "  HTTP Test Driver  "
                }
                """.formatted(rawPassword));

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body())
                .contains("HTTP Test Driver")
                .contains("ACTIVE")
                .contains("DRIVER")
                .doesNotContain(rawPassword);

        StoredAccount account = jdbc.queryForObject("""
                SELECT u.user_id, u.email_normalized, u.display_name,
                       u.account_status, c.password_hash
                FROM users u
                JOIN user_credentials c ON c.user_id = u.user_id
                WHERE u.email_normalized = ?
                """,
                (resultSet, rowNumber) -> new StoredAccount(
                        resultSet.getObject("user_id", UUID.class),
                        resultSet.getString("email_normalized"),
                        resultSet.getString("display_name"),
                        resultSet.getString("account_status"),
                        resultSet.getString("password_hash")),
                "driver.http@example.com");

        Integer uuidVersion = jdbc.queryForObject(
                "SELECT uuid_extract_version(?)",
                Integer.class,
                account.userId());
        Integer driverRoleCount = jdbc.queryForObject("""
                SELECT count(*)
                FROM user_roles
                WHERE user_id = ? AND role_code = 'DRIVER'
                """,
                Integer.class,
                account.userId());

        assertThat(uuidVersion).isEqualTo(7);
        assertThat(account.email()).isEqualTo("driver.http@example.com");
        assertThat(account.displayName()).isEqualTo("HTTP Test Driver");
        assertThat(account.status()).isEqualTo("ACTIVE");
        assertThat(account.passwordHash())
                .startsWith("{argon2@SpringSecurity_v5_8}$argon2id$")
                .doesNotContain(rawPassword);
        assertThat(driverRoleCount).isEqualTo(1);
    }

    @Test
    void returnsConflictWhenNormalizedEmailAlreadyExists() throws Exception {
        String body = """
                {
                  "email": "duplicate@example.com",
                  "password": "a valid duplicate passphrase",
                  "displayName": "First User"
                }
                """;
        assertThat(postRegistration(body).statusCode()).isEqualTo(201);

        HttpResponse<String> duplicateResponse = postRegistration("""
                {
                  "email": " DUPLICATE@EXAMPLE.COM ",
                  "password": "another valid long passphrase",
                  "displayName": "Second User"
                }
                """);

        assertThat(duplicateResponse.statusCode()).isEqualTo(409);
        String correlationId = duplicateResponse.headers()
                .firstValue(RequestCorrelationFilter.RESPONSE_HEADER)
                .orElseThrow();
        assertThat(duplicateResponse.body())
                .contains("EMAIL_ALREADY_REGISTERED")
                .contains(correlationId)
                .doesNotContain("constraint")
                .doesNotContain("SQL");

        Integer accountCount = jdbc.queryForObject(
                "SELECT count(*) FROM users WHERE email_normalized = 'duplicate@example.com'",
                Integer.class);
        assertThat(accountCount).isEqualTo(1);
    }

    @Test
    void rejectsPolicyInvalidPasswordWithoutCreatingAnAccount() throws Exception {
        HttpResponse<String> response = postRegistration("""
                {
                  "email": "short-password@example.com",
                  "password": "too short",
                  "displayName": "Short Password"
                }
                """);

        assertThat(response.statusCode()).isEqualTo(422);
        assertThat(response.body())
                .contains("REGISTRATION_VALIDATION_FAILED")
                .contains("password")
                .doesNotContain("too short");

        Integer accountCount = jdbc.queryForObject(
                "SELECT count(*) FROM users WHERE email_normalized = 'short-password@example.com'",
                Integer.class);
        assertThat(accountCount).isZero();
    }

    @Test
    void rejectsMalformedJsonWithTheStableProblemContract() throws Exception {
        HttpResponse<String> response = postRegistration("{not-valid-json");

        assertThat(response.statusCode()).isEqualTo(400);
        String correlationId = response.headers()
                .firstValue(RequestCorrelationFilter.RESPONSE_HEADER)
                .orElseThrow();
        assertThat(response.body())
                .contains("MALFORMED_REQUEST")
                .contains(correlationId)
                .doesNotContain("HttpMessageNotReadableException");
    }

    @Test
    void transactionalCreatorRollsBackUserWhenCredentialInsertFails() {
        String email = "transaction-proxy-rollback@example.com";

        assertThatThrownBy(() -> accountCreator.create(
                new EmailAddress(email),
                "Rollback User",
                "   "))
                .isInstanceOf(DataIntegrityViolationException.class);

        Integer accountCount = jdbc.queryForObject(
                "SELECT count(*) FROM users WHERE email_normalized = ?",
                Integer.class,
                email);
        assertThat(accountCount).isZero();
    }

    private HttpResponse<String> postRegistration(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + serverPort + "/api/v1/auth/registrations"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private record StoredAccount(
            UUID userId,
            String email,
            String displayName,
            String status,
            String passwordHash) {
    }
}
