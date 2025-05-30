package com.example.mygpt.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataSourceIntegrationTest {

    @Autowired
    private DataSource dataSource;

    // Ce test vérifie que la connexion à la base de données PostgreSQL est correctement
    // configurée en s'assurant que le DataSource est initialisé et qu'une requête simple
    // peut être exécutée avec succès.
    @Test
    void shouldConnectToPostgreSQLDatabase() {
        assertNotNull(dataSource, "DataSource should be initialized");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result, "Should execute a simple query successfully");
    }
}