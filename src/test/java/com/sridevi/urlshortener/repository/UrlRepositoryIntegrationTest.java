package com.sridevi.urlshortener.repository;

import com.sridevi.urlshortener.entity.Url;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UrlRepositoryIntegrationTest {
    @Container @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired UrlRepository repository;

    @Test void flywaySchemaSupportsSequenceFirstInsertAndSoftDeleteLookup() {
        long id = repository.nextSequenceValue();
        Url url = new Url();
        url.setId(id); url.setShortCode("tc123"); url.setOriginalUrl("https://example.com"); url.setCustomAlias(false);
        repository.saveAndFlush(url);
        assertTrue(repository.findByShortCodeAndDeletedFalse("tc123").isPresent());
        url.setDeleted(true); repository.saveAndFlush(url);
        assertTrue(repository.findByShortCodeAndDeletedFalse("tc123").isEmpty());
    }
}
