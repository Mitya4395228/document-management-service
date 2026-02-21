package com.example.management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import com.example.management.dto.DocumentApproval;
import com.example.management.dto.DocumentCreateDTO;
import com.example.management.dto.DocumentFilter;
import com.example.management.dto.DocumentBatchReceipt;
import com.example.management.dto.DocumentPersistentApproval;
import com.example.management.dto.DocumentSubmit;
import com.example.management.dto.enums.ResultType;
import com.example.management.entity.ApprovalRegistryEntity;
import com.example.management.entity.DocumentEntity;
import com.example.management.entity.DocumentStatusHistoryEntity;
import com.example.management.entity.enums.Action;
import com.example.management.entity.enums.DocumentStatus;
import com.example.management.exception.EntityNotFoundException;
import com.example.management.repository.ApprovalRegistryRepository;
import com.example.management.repository.DocumentRepository;
import com.example.management.repository.DocumentStatusHistoryRepository;
import com.example.management.TestcontainersConfiguration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class DocumentServiceTest {

    @Autowired
    DocumentService documentService;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentStatusHistoryRepository documentStatusHistoryRepository;

    @Autowired
    ApprovalRegistryRepository approvalRegistryRepository;

    @BeforeEach
    void setUp() {
        approvalRegistryRepository.deleteAll();
        documentStatusHistoryRepository.deleteAll();
        documentRepository.deleteAll();
    }

    @Test
    void testGetDocument() {

        var expected = createDocument(DocumentStatus.DRAFT);
        var actual = documentService.getDocument(expected.getId());

        Assertions.assertThat(actual).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("statusHistory").isEqualTo(expected);
        assertTrue(actual.statusHistory().size() == 1);
        assertTrue(expected.getStatusHistory().size() == 1);
        Assertions.assertThat(actual.statusHistory())
                .usingRecursiveFieldByFieldElementComparator(configRecursiveComparison())
                .isEqualTo(expected.getStatusHistory());
    }

    @Test
    void testGetDocument_EntityNotFound() {
        assertThrows(EntityNotFoundException.class, () -> documentService.getDocument(UUID.randomUUID()));
    }

    @Test
    void testGetDocuments() {

        var expected = createDocument(DocumentStatus.DRAFT);
        var pageable = PageRequest.of(0, 20);
        var actual = documentService.getDocuments(new DocumentBatchReceipt(Set.of(expected.getId())), pageable);

        assertTrue(actual.getContent().size() == 1);
        assertTrue(actual.getMetadata().number() == pageable.getPageNumber());
        assertTrue(actual.getMetadata().size() == pageable.getPageSize());
        assertTrue(actual.getMetadata().totalPages() == 1);
        assertTrue(actual.getMetadata().totalElements() == 1);
        Assertions.assertThat(actual.getContent().getFirst()).usingRecursiveComparison(configRecursiveComparison())
                .isEqualTo(expected);
    }

    @Test
    void testCreate() {

        var expected1 = Instancio.create(DocumentCreateDTO.class);
        var actual1 = documentService.create(expected1);

        assertEquals(expected1.author(), actual1.author());
        assertEquals(expected1.title(), actual1.title());
        assertTrue(actual1.createdAt().isBefore(LocalDateTime.now()));
        assertTrue(actual1.updatedAt().isBefore(LocalDateTime.now()));
        assertTrue(actual1.status().equals(DocumentStatus.DRAFT));

        var expected2 = Instancio.create(DocumentCreateDTO.class);
        var actual2 = documentService.create(expected2);
        var expected3 = Instancio.create(DocumentCreateDTO.class);
        var actual3 = documentService.create(expected3);
        assertNotEquals(actual1.number(), actual2.number());
        assertNotEquals(actual1.number(), actual3.number());
        assertNotEquals(actual2.number(), actual3.number());
        assertTrue(actual3.number() ==  actual2.number() + 1);
        assertTrue(actual2.number() == actual1.number() + 1);
    }

    @Test
    void testGetAllByFilter() {

        var expected1 = createDocument(DocumentStatus.DRAFT);
        var expected2 = createDocument(DocumentStatus.SUBMITTED);
        var expected3 = createDocument(DocumentStatus.APPROVED);
        var pageable = PageRequest.of(0, 10);

        var actual_full = documentService.getAllByFilter(new DocumentFilter(null, null, null, null, null), pageable);

        assertTrue(actual_full.getMetadata().number() == pageable.getPageNumber());
        assertTrue(actual_full.getMetadata().size() == pageable.getPageSize());
        assertTrue(actual_full.getMetadata().totalPages() == 1);
        assertTrue(actual_full.getMetadata().totalElements() == 3);
        assertTrue(actual_full.getContent().size() == 3);
        Assertions.assertThat(actual_full.getContent()).usingRecursiveComparison(configRecursiveComparison())
                .isEqualTo(List.of(expected1, expected2, expected3));

        var actual_filter = documentService.getAllByFilter(new DocumentFilter(expected1.getAuthor(),
                expected1.getTitle(), expected1.getStatus(), expected1.getCreatedAt(), expected1.getCreatedAt()),
                pageable);
        assertTrue(actual_filter.getMetadata().number() == pageable.getPageNumber());
        assertTrue(actual_filter.getMetadata().size() == pageable.getPageSize());
        assertTrue(actual_filter.getMetadata().totalPages() == 1);
        assertTrue(actual_filter.getMetadata().totalElements() == 1);
        assertTrue(actual_filter.getContent().size() == 1);
        Assertions.assertThat(actual_filter.getContent()).usingRecursiveComparison(configRecursiveComparison())
                .isEqualTo(List.of(expected1));

    }

    @Test
    void testSubmit_SUCCESS() {

        var expected = createDocument(DocumentStatus.DRAFT, false);
        var submit = Instancio.of(DocumentSubmit.class).set(Select.field("ids"), Set.of(expected.getId())).create();
        var result = documentService.submit(submit);
        var actual = documentRepository.findWithHistoryById(expected.getId()).get();

        assertEquals(expected.getStatus(), DocumentStatus.DRAFT);
        assertTrue(expected.getStatusHistory().isEmpty());

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), expected.getId());
        assertEquals(result.get(0).message(), ResultType.SUCCESS);

        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getNumber(), expected.getNumber());
        assertEquals(actual.getAuthor(), expected.getAuthor());
        assertEquals(actual.getTitle(), expected.getTitle());
        assertEquals(actual.getStatus(), DocumentStatus.SUBMITTED);
        assertTrue(actual.getStatusHistory().size() == 1);
        assertEquals(actual.getCreatedAt(), expected.getCreatedAt());
        assertTrue(actual.getUpdatedAt().isAfter(expected.getUpdatedAt()));
        assertTrue(actual.getVersion() > expected.getVersion());

        var history = actual.getStatusHistory().iterator().next();
        assertEquals(history.getAction(), Action.SUBMIT);
        assertEquals(history.getInitiator(), submit.initiator());
        assertEquals(history.getComment(), submit.comment());
    }

    @Test
    void testSubmit_DOCUMENT_NOT_FOUND() {

        var id = UUID.randomUUID();
        var submit = Instancio.of(DocumentSubmit.class).set(Select.field("ids"), Set.of(id)).create();
        var result = documentService.submit(submit);

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), id);
        assertEquals(result.get(0).message(), ResultType.DOCUMENT_NOT_FOUND);
    }

    @Test
    void testSubmit_CONFLICT() {

        var expected = createDocument(DocumentStatus.SUBMITTED);
        var submit = Instancio.of(DocumentSubmit.class).set(Select.field("ids"), Set.of(expected.getId())).create();
        var result = documentService.submit(submit);
        var actual = documentRepository.findWithHistoryById(expected.getId()).get();

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), expected.getId());
        assertEquals(result.get(0).message(), ResultType.CONFLICT);

        Assertions.assertThat(actual).usingRecursiveComparison(configRecursiveComparison()).isEqualTo(expected);
    }

    @Test
    void testSubmit_CONFLICT_2() {

        var expected = createDocument(DocumentStatus.DRAFT);
        var submit = Instancio.of(DocumentSubmit.class).set(Select.field("ids"), Set.of(expected.getId())).create();
        var result_first = documentService.submit(submit);
        var actual_first = documentRepository.findWithHistoryById(expected.getId()).get();

        assertTrue(result_first.size() == 1);
        assertEquals(result_first.get(0).id(), expected.getId());
        assertEquals(result_first.get(0).message(), ResultType.SUCCESS);
        assertEquals(actual_first.getStatus(), DocumentStatus.SUBMITTED);

        var result_second = documentService.submit(submit);
        var actual_second = documentRepository.findWithHistoryById(expected.getId()).get();

        assertTrue(result_second.size() == 1);
        assertEquals(result_second.get(0).id(), expected.getId());
        assertEquals(result_second.get(0).message(), ResultType.CONFLICT);
        assertEquals(actual_second.getStatus(), DocumentStatus.SUBMITTED);

        Assertions.assertThat(actual_first).usingRecursiveComparison(configRecursiveComparison())
                .isEqualTo(actual_second);
    }

    @Test
    void testApprove_SUCCESS() {

        var expected = createDocument(DocumentStatus.SUBMITTED, false);
        var registry = approvalRegistryRepository.findByDocumentId(expected.getId()).orElse(null);
        var approval = Instancio.of(DocumentApproval.class).set(Select.field("ids"), Set.of(expected.getId())).create();
        var result = documentService.approve(approval);
        var actual = documentRepository.findWithHistoryById(expected.getId()).get();

        assertEquals(expected.getStatus(), DocumentStatus.SUBMITTED);
        assertTrue(expected.getStatusHistory().isEmpty());

        assertNull(registry);

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), expected.getId());
        assertEquals(result.get(0).message(), ResultType.SUCCESS);

        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getNumber(), expected.getNumber());
        assertEquals(actual.getAuthor(), expected.getAuthor());
        assertEquals(actual.getTitle(), expected.getTitle());
        assertEquals(actual.getStatus(), DocumentStatus.APPROVED);
        assertTrue(actual.getStatusHistory().size() == 1);
        assertEquals(actual.getCreatedAt(), expected.getCreatedAt());
        assertTrue(actual.getUpdatedAt().isAfter(expected.getUpdatedAt()));
        assertTrue(actual.getVersion() > expected.getVersion());

        var history = actual.getStatusHistory().iterator().next();
        assertEquals(history.getAction(), Action.APPROVE);
        assertEquals(history.getInitiator(), approval.initiator());
        assertEquals(history.getComment(), approval.comment());

        registry = approvalRegistryRepository.findByDocumentId(expected.getId()).get();
        assertNotNull(registry);
        assertEquals(registry.getApprover(), approval.initiator());
    }

    @Test
    void testApprove_DOCUMENT_NOT_FOUND() {

        var id = UUID.randomUUID();

        var approval = Instancio.of(DocumentApproval.class).set(Select.field("ids"), Set.of(id)).create();
        var result = documentService.approve(approval);

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), id);
        assertEquals(result.get(0).message(), ResultType.DOCUMENT_NOT_FOUND);

        var registry = approvalRegistryRepository.findByDocumentId(id).orElse(null);
        assertNull(registry);
    }

    @Test
    void testApprove_CONFLICT() {

        var expected = createDocument(DocumentStatus.APPROVED, false);
        var approval = Instancio.of(DocumentApproval.class).set(Select.field("ids"), Set.of(expected.getId())).create();
        var result = documentService.approve(approval);
        var actual = documentRepository.findWithHistoryById(expected.getId()).get();

        var registry = approvalRegistryRepository.findByDocumentId(expected.getId()).orElse(null);
        assertNull(registry);

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), expected.getId());
        assertEquals(result.get(0).message(), ResultType.CONFLICT);

        Assertions.assertThat(actual).usingRecursiveComparison(configRecursiveComparison()).isEqualTo(expected);
    }

    @Test
    void testApprove_REGISTRY_REGISTRATION_ERROR() {

        var expected = createDocument(DocumentStatus.SUBMITTED, false);

        var registry_expected = new ApprovalRegistryEntity();
        registry_expected.setDocument(expected);
        registry_expected.setApprover("approver");
        registry_expected = approvalRegistryRepository.save(registry_expected);

        var approval = Instancio.of(DocumentApproval.class).set(Select.field("ids"), Set.of(expected.getId())).create();
        var result = documentService.approve(approval);
        var actual = documentRepository.findWithHistoryById(expected.getId()).get();

        assertTrue(result.size() == 1);
        assertEquals(result.get(0).id(), expected.getId());
        assertEquals(result.get(0).message(), ResultType.REGISTRY_REGISTRATION_ERROR);

        Assertions.assertThat(actual).usingRecursiveComparison(configRecursiveComparison()).isEqualTo(expected);

        var registry_actual = approvalRegistryRepository.findByDocumentId(expected.getId()).orElse(null);
        Assertions.assertThat(registry_actual).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("document")
                .isEqualTo(registry_expected);
    }

    @Test
    void testPersistentApprove() throws InterruptedException {

        var expected = createDocument(DocumentStatus.SUBMITTED, false);
        assertTrue(expected.getStatusHistory().isEmpty());

        var registry = approvalRegistryRepository.findByDocumentId(expected.getId()).orElse(null);
        assertNull(registry);

        var attempts = 1000;
        var approval = Instancio.of(DocumentPersistentApproval.class)
                .set(Select.field("id"), expected.getId())
                .set(Select.field("threads"), 5)
                .set(Select.field("attempts"), attempts)
                .create();

        var result = documentService.persistentApprove(approval);

        assertEquals(result.id(), expected.getId());
        assertEquals(result.status(), DocumentStatus.APPROVED);
        assertTrue(result.results().values().stream().mapToInt(AtomicInteger::get).sum() == attempts);
        assertTrue(result.results().get(ResultType.SUCCESS).get() == 1);

        expected = documentRepository.findWithHistoryById(approval.id()).get();
        assertTrue(expected.getStatusHistory().size() == 1);

        registry = approvalRegistryRepository.findByDocumentId(expected.getId()).orElse(null);
        assertNotNull(registry);
    }

    private DocumentEntity createDocument(DocumentStatus status) {
        return createDocument(status, true);
    }

    private DocumentEntity createDocument(DocumentStatus status, boolean statusHistory) {
        var doc = Instancio.of(DocumentEntity.class)
                .ignore(Select.all(LocalDateTime.class))
                .ignore(Select.all(UUID.class))
                .ignore(Select.allInts())
                .ignore(Select.allLongs())
                .ignore(Select.all(Set.class))
                .set(Select.field(DocumentEntity::getStatus), status)
                .create();
        if (statusHistory) {
            var history = Instancio.of(DocumentStatusHistoryEntity.class)
                    .ignore(Select.all(LocalDateTime.class))
                    .ignore(Select.all(UUID.class))
                    .create();
            history.setDocument(doc);
            doc.setStatusHistory(Set.of(history));
        }
        doc = documentRepository.save(doc);
        return documentRepository.findWithHistoryById(doc.getId()).get();
    }

    private RecursiveComparisonConfiguration configRecursiveComparison() {
        return RecursiveComparisonConfiguration.builder()
                .withEqualsForType(
                        (t1, t2) -> t1.truncatedTo(ChronoUnit.SECONDS).equals(t2.truncatedTo(ChronoUnit.SECONDS)),
                        LocalDateTime.class)
                .build();
    }
}
