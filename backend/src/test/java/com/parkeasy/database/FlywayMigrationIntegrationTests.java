package com.parkeasy.database;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
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

        assertThat(successfulMigrationCount).isEqualTo(1);
        assertThat(btreeGistInstalled).isTrue();
    }
}
