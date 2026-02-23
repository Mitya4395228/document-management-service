package com.example.common.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import com.example.common.dto.enums.DocumentStatus;

public record DocumentReadExtendedDTO(UUID id, Long number, String author, String title, DocumentStatus status,
        Set<DocumentStatusHistoryReadDTO> statusHistory, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
