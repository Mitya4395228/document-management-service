package com.example.management.service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Service;

import com.example.management.dto.DocumentApprovalResult;
import com.example.management.dto.DocumentApproval;
import com.example.management.dto.DocumentCreateDTO;
import com.example.management.dto.DocumentFilter;
import com.example.management.dto.DocumentBatchReceipt;
import com.example.management.dto.DocumentPersistentApproval;
import com.example.management.dto.DocumentReadDTO;
import com.example.management.dto.DocumentReadExtendedDTO;
import com.example.management.dto.DocumentSubmit;
import com.example.management.dto.DocumentPersistentApprovalResult;
import com.example.management.dto.DocumentSubmitResult;
import com.example.management.dto.enums.ResultType;
import com.example.management.exception.EntityNotFoundException;
import com.example.management.exception.RegistryRegistrationException;
import com.example.management.exception.UnacceptableStatusException;
import com.example.management.mapper.DocumentMapper;
import com.example.management.repository.DocumentRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Document service: reading one or a package documents, search by filter,
 * creating, submitting, approving, persistent approving.
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class DocumentService {

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    DocumentProcessor documentProcessor;

    @Autowired
    DocumentMapper mapper;

    /**
     * Get document with history by id from database.
     * 
     * @param id {@code UUID}
     * @return {@link DocumentReadExtendedDTO}
     * @throws EntityNotFoundException if document with such id does not exist
     */
    public DocumentReadExtendedDTO getDocument(UUID id) {
        return mapper.entityToExtendedDTO(
                documentRepository.findWithHistoryById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Document not found")));
    }

    /**
     * Batch receipt of documents with pagination and sorting
     * 
     * @param batch    {@link DocumentBatchReceipt}
     * @param pageable {@link Pageable}
     * @return {@link PagedModel} of {@link DocumentReadDTO}
     */
    public PagedModel<DocumentReadDTO> getDocuments(DocumentBatchReceipt batch, Pageable pageable) {
        var totalElements = batch.ids().size();
        var content = documentRepository.findAllById(batch.ids(), pageable).stream().map(mapper::entityToDTO).toList();
        return new PagedModel<>(new PageImpl<>(content, pageable, totalElements));
    }

    /**
     * Get all documents by filter with pagination and sorting
     *
     * @param filter   {@link DocumentFilter}
     * @param pageable {@link Pageable}
     * @return {@link PagedModel} of {@link DocumentReadDTO}
     */
    public PagedModel<DocumentReadDTO> getAllByFilter(DocumentFilter filter, Pageable pageable) {
        return documentRepository.finadAllByFilter(filter, pageable);
    }

    /**
     * Create document. Number of document is generated automatically.
     * 
     * @param createDto {@link DocumentCreateDTO}
     * @return {@link DocumentReadDTO}
     */
    public DocumentReadDTO create(DocumentCreateDTO createDto) {
        var entity = mapper.createDtoToEntity(createDto);
        entity = documentRepository.save(entity);
        return mapper.entityToDTO(documentRepository.findById(entity.getId()).get());
    }

    /**
     * Batch submit documents. Size of batch is limited to 1000.
     * 
     * @param submit {@link DocumentSubmit}
     * @return {@link List} of {@link DocumentSubmitResult}
     */
    public List<DocumentSubmitResult> submit(DocumentSubmit submit) {
        return submit.ids().stream().map(id -> {
            try {
                documentProcessor.submit(id, submit.initiator(), submit.comment());
                return new DocumentSubmitResult(id, ResultType.SUCCESS);
            } catch (EntityNotFoundException e) {
                return new DocumentSubmitResult(id, ResultType.DOCUMENT_NOT_FOUND);
            } catch (UnacceptableStatusException | OptimisticLockingFailureException e) {
                return new DocumentSubmitResult(id, ResultType.CONFLICT);
            } catch (Exception e) {
                return new DocumentSubmitResult(id, ResultType.ERROR);
            }
        }).toList();
    }

    /**
     * Batch approve documents. Size of batch is limited to 1000. 
     * 
     * @param approval {@link DocumentApproval}
     * @return {@link List} of {@link DocumentApprovalResult}
     */
    public List<DocumentApprovalResult> approve(DocumentApproval approval) {
        return approval.ids().stream().map(id -> {
            try {
                documentProcessor.approve(id, approval.initiator(), approval.comment());
                return new DocumentApprovalResult(id, ResultType.SUCCESS);
            } catch (EntityNotFoundException e) {
                return new DocumentApprovalResult(id, ResultType.DOCUMENT_NOT_FOUND);
            } catch (UnacceptableStatusException | OptimisticLockingFailureException e) {
                return new DocumentApprovalResult(id, ResultType.CONFLICT);
            } catch (RegistryRegistrationException e) {
                return new DocumentApprovalResult(id, ResultType.REGISTRY_REGISTRATION_ERROR);
            } catch (Exception e) {
                return new DocumentApprovalResult(id, ResultType.ERROR);
            }
        }).toList();
    }

    /**
     * Approve document. In multithread with multiple attempts to approve document.
     * 
     * @param approval {@link DocumentPersistentApproval}
     * @return {@link DocumentPersistentApprovalResult}
     * @throws InterruptedException
     */
    public DocumentPersistentApprovalResult persistentApprove(DocumentPersistentApproval approval)
            throws InterruptedException {

        var excecutor = Executors.newFixedThreadPool(approval.threads());

        var results = new HashMap<ResultType, AtomicInteger>();
        Stream.of(ResultType.values()).forEach(type -> results.put(type, new AtomicInteger()));

        List<Callable<Void>> tasks = IntStream.range(0, approval.attempts())
                .mapToObj(i -> (Callable<Void>) () -> {
                    try {
                        documentProcessor.approve(approval.id(), approval.initiator(), approval.comment());
                        results.get(ResultType.SUCCESS).incrementAndGet();
                    } catch (EntityNotFoundException e) {
                        results.get(ResultType.DOCUMENT_NOT_FOUND).incrementAndGet();
                    } catch (UnacceptableStatusException | OptimisticLockingFailureException e) {
                        results.get(ResultType.CONFLICT).incrementAndGet();
                    } catch (RegistryRegistrationException e) {
                        results.get(ResultType.REGISTRY_REGISTRATION_ERROR).incrementAndGet();
                    } catch (Exception e) {
                        results.get(ResultType.ERROR).incrementAndGet();
                    }
                    return null;
                })
                .toList();

        excecutor.invokeAll(tasks);
        excecutor.shutdown();

        return new DocumentPersistentApprovalResult(approval.id(), results, getDocument(approval.id()).status());
    }

}
