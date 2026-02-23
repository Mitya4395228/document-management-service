package com.example.generator.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import com.example.common.dto.enums.DocumentStatus;
import com.example.generator.repository.DocumentRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * DocumentService: find documents by status
 */

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class DocumentService {

    DocumentRepository repository;

    public DocumentService(DocumentRepository repository) {
        this.repository = repository;
    }

    /**
     * Find documents id by status with limit
     * 
     * @param status {@link DocumentStatus}
     * @param limit  {@code int}
     * @return {@code Set<UUID>}
     */
    public Set<UUID> findIds(DocumentStatus status, int limit) {
        return repository.findIds(status, Limit.of(limit));
    }

    /**
     * Counts the number of documents with the specified status.
     *
     * @param status the {@link DocumentStatus} to filter documents by
     * @return the total {@code long} count of documents matching the given status
     */
    public long count(DocumentStatus status) {
        return repository.countByStatus(status);
    }

}
