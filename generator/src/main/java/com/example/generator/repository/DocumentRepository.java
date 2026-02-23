package com.example.generator.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.common.dto.enums.DocumentStatus;
import com.example.generator.entity.DocumentEntity;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    @Query("SELECT id FROM DocumentEntity WHERE status = :status ORDER BY updatedAt")
    Set<UUID> findIds(DocumentStatus status, Limit limit);

    long countByStatus(DocumentStatus status);

}
