package com.example.management.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.management.entity.ApprovalRegistryEntity;

public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistryEntity, UUID> {

    Optional<ApprovalRegistryEntity> findByDocumentId(UUID documentId);

}
