package com.example.management.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.management.entity.enums.DocumentStatus;

public record DocumentReadDTO(UUID id, Long number, String author, String title, DocumentStatus status, LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
