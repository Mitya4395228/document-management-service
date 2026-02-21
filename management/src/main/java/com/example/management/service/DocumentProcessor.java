package com.example.management.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.management.entity.ApprovalRegistryEntity;
import com.example.management.entity.enums.DocumentStatus;
import com.example.management.exception.EntityNotFoundException;
import com.example.management.exception.RegistryRegistrationException;
import com.example.management.exception.UnacceptableStatusException;
import com.example.management.mapper.DocumentMapper;
import com.example.management.repository.ApprovalRegistryRepository;
import com.example.management.repository.DocumentRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Service for atomic document processing
 */

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class DocumentProcessor {

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    ApprovalRegistryRepository registryRepository;

    @Autowired
    DocumentMapper mapper;

    /**
     * Submit document and records document status change
     * 
     * @param id {@code UUID}
     * @param initiator {@code String}
     * @param comment {@code String}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submit(UUID id, String initiator, String comment) {

        var document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        if (document.getStatus() != DocumentStatus.DRAFT) {
            throw new UnacceptableStatusException("Document is not in draft status");
        }

        document = mapper.changeStatus(document, DocumentStatus.SUBMITTED, initiator, comment);
        documentRepository.save(document);
    }

    /**
     * Approve document, records approval registry and status history
     * 
     * @param id {@code UUID}
     * @param initiator {@code String}
     * @param comment {@code String}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approve(UUID id, String initiator, String comment) {

        var document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        if (document.getStatus() != DocumentStatus.SUBMITTED) {
            throw new UnacceptableStatusException("Document status is not submitted");
        }

        document = mapper.changeStatus(document, DocumentStatus.APPROVED, initiator, comment);
        documentRepository.saveAndFlush(document);

        try {
            var registry = new ApprovalRegistryEntity();
            registry.setDocument(document);
            registry.setApprover(initiator);
            registryRepository.saveAndFlush(registry);
        } catch (Exception e) {
            throw new RegistryRegistrationException("Error while registering registry");
        }
    }

}
