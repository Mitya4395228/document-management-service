package com.example.management.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.example.management.entity.enums.DocumentStatus;

public record DocumentReadExtendedDTO(UUID id, Long number, String author, String title, DocumentStatus status,
        Set<DocumentStatusHistoryReadDTO> statusHistory, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
