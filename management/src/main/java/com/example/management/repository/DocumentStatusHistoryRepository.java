package com.example.management.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.management.entity.DocumentStatusHistoryEntity;

public interface DocumentStatusHistoryRepository  extends JpaRepository<DocumentStatusHistoryEntity, UUID> {

}
