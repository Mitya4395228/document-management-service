package com.example.generator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.common.dto.enums.DocumentStatus;
import com.example.generator.TestcontainersConfiguration;
import com.example.generator.entity.DocumentEntity;
import com.example.generator.job.DocumentTasks;
import com.example.generator.job.GeneratorRunner;
import com.example.generator.repository.DocumentRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class DocumentServiceTest {

    @Autowired
    DocumentService service;

    @Autowired
    DocumentRepository repository;

    @MockitoBean
    DocumentTasks tasks;

    @MockitoBean
    GeneratorRunner runner;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void testCount() {

        assertEquals(0, service.count(DocumentStatus.DRAFT));
        assertEquals(0, service.count(DocumentStatus.SUBMITTED));
        assertEquals(0, service.count(DocumentStatus.APPROVED));

        createDocument(DocumentStatus.DRAFT);
        createDocument(DocumentStatus.SUBMITTED);
        createDocument(DocumentStatus.APPROVED);

        assertEquals(1, service.count(DocumentStatus.DRAFT));
        assertEquals(1, service.count(DocumentStatus.SUBMITTED));
        assertEquals(1, service.count(DocumentStatus.APPROVED));

    }

    @Test
    void testFindIds() {

        assertTrue(service.findIds(DocumentStatus.DRAFT, 100).isEmpty());
        assertTrue(service.findIds(DocumentStatus.SUBMITTED, 100).isEmpty());
        assertTrue(service.findIds(DocumentStatus.APPROVED, 100).isEmpty());

        createDocument(DocumentStatus.DRAFT);
        createDocument(DocumentStatus.SUBMITTED);
        createDocument(DocumentStatus.APPROVED);

        assertEquals(1, service.findIds(DocumentStatus.DRAFT, 100).size());
        assertEquals(1, service.findIds(DocumentStatus.SUBMITTED, 100).size());
        assertEquals(1, service.findIds(DocumentStatus.APPROVED, 100).size());

    }

    private DocumentEntity createDocument(DocumentStatus status) {
        var doc = Instancio.of(DocumentEntity.class)
                .ignore(Select.all(UUID.class))
                .ignore(Select.allInts())
                .ignore(Select.allLongs())
                .set(Select.field(DocumentEntity::getStatus), status)
                .create();
        return doc = repository.save(doc);
    }

}
