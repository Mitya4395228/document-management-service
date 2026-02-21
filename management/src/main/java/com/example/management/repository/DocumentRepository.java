package com.example.management.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.management.entity.DocumentEntity;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID>, DocumentRepositoryCustom {

    @Query("SELECT d FROM DocumentEntity d WHERE d.id IN :ids")
    List<DocumentEntity> findAllById(Set<UUID> ids, Pageable pageable);

    @EntityGraph(attributePaths = {"statusHistory"})
    @Query("SELECT d FROM DocumentEntity d WHERE d.id = :id")
    Optional<DocumentEntity> findWithHistoryById(UUID id);

}
